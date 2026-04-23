package com.cupk.healthy_diet.service.impl;

import com.cupk.healthy_diet.config.BaiduDishConfig;
import com.cupk.healthy_diet.exception.BusinessException;
import com.cupk.healthy_diet.service.DishRecognitionService;
import com.cupk.healthy_diet.vo.DishRecognitionVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BaiduDishRecognitionService implements DishRecognitionService {

    private final BaiduDishConfig baiduDishConfig;
    private final OkHttpClient okHttpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String cachedToken;
    private long tokenExpiresAt;

    @Override
    public DishRecognitionVO recognize(MultipartFile file) {
        validateFile(file);

        try {
            String imageBase64 = Base64.getEncoder().encodeToString(file.getBytes());
            String token = getAccessToken();
            String url = baiduDishConfig.getDishUrl() + "?access_token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);

            RequestBody body = new FormBody.Builder(StandardCharsets.UTF_8)
                    .add("image", imageBase64)
                    .add("top_num", "5")
                    .add("filter_threshold", "0.7")
                    .add("baike_num", "0")
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            try (Response response = okHttpClient.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";
                if (!response.isSuccessful()) {
                    throw new BusinessException("菜品识别服务暂时不可用");
                }
                return parseRecognitionResult(responseBody);
            }
        } catch (IOException e) {
            throw new BusinessException("菜品识别失败，请稍后重试");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("图片不能为空");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("只支持图片文件");
        }
    }

    private String getAccessToken() throws IOException {
        long now = Instant.now().getEpochSecond();
        if (cachedToken != null && tokenExpiresAt - 60 > now) {
            return cachedToken;
        }

        String url = baiduDishConfig.getTokenUrl()
                + "?grant_type=client_credentials"
                + "&client_id=" + URLEncoder.encode(baiduDishConfig.getApiKey(), StandardCharsets.UTF_8)
                + "&client_secret=" + URLEncoder.encode(baiduDishConfig.getSecretKey(), StandardCharsets.UTF_8);

        Request request = new Request.Builder().url(url).get().build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            JsonNode root = objectMapper.readTree(responseBody);
            if (!response.isSuccessful() || !root.has("access_token")) {
                throw new BusinessException("百度访问令牌获取失败");
            }

            cachedToken = root.get("access_token").asText();
            long expiresIn = root.has("expires_in") ? root.get("expires_in").asLong() : 2592000;
            tokenExpiresAt = now + expiresIn;
            return cachedToken;
        }
    }

    private DishRecognitionVO parseRecognitionResult(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        if (root.has("error_code")) {
            String message = root.has("error_msg") ? root.get("error_msg").asText() : "菜品识别失败";
            throw new BusinessException("菜品识别失败: " + message);
        }

        JsonNode result = root.get("result");
        if (result == null || !result.isArray() || result.isEmpty()) {
            throw new BusinessException("未识别到菜品，请换一张更清晰的图片");
        }

        List<DishRecognitionVO.DishItemVO> items = new ArrayList<>();
        for (JsonNode itemNode : result) {
            DishRecognitionVO.DishItemVO item = new DishRecognitionVO.DishItemVO();
            item.setName(itemNode.path("name").asText("未知菜品"));
            item.setCalorie(itemNode.path("calorie").asText(""));
            item.setProbability(itemNode.path("probability").asDouble(0));
            items.add(item);
        }

        DishRecognitionVO vo = new DishRecognitionVO();
        vo.setItems(items);
        DishRecognitionVO.DishItemVO best = items.get(0);
        vo.setBestName(best.getName());
        vo.setBestCalories(best.getCalorie());
        vo.setBestProbability(best.getProbability());
        return vo;
    }
}
