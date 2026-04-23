package com.cupk.healthy_diet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cupk.healthy_diet.config.SparkConfig;
import com.cupk.healthy_diet.entity.DietRecord;
import com.cupk.healthy_diet.entity.HealthInfo;
import com.cupk.healthy_diet.mapper.DietRecordMapper;
import com.cupk.healthy_diet.mapper.HealthInfoMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SparkChatService {

    private static final String DEFAULT_HOST = "spark-api.xf-yun.com";
    private static final String DEFAULT_PATH = "/x2";
    private static final String DEFAULT_DOMAIN = "spark-x";

    private final SparkConfig sparkConfig;
    private final HealthInfoMapper healthInfoMapper;
    private final DietRecordMapper dietRecordMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public interface StreamCallback {
        void onMessage(String content);
        void onComplete();
        void onError(String error);
    }

    public String chat(Integer userId, String userMessage, List<Map<String, String>> history) {
        CountDownLatch latch = new CountDownLatch(1);
        StringBuilder answer = new StringBuilder();
        streamChat(userId, userMessage, history, new StreamCallback() {
            @Override
            public void onMessage(String content) {
                answer.append(content);
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }

            @Override
            public void onError(String error) {
                if (answer.isEmpty()) {
                    answer.append(error);
                }
                latch.countDown();
            }
        });

        try {
            latch.await(90, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return answer.isEmpty() ? "抱歉，我现在无法回答你的问题，请稍后再试。" : answer.toString();
    }

    public void streamChat(Integer userId, String userMessage, List<Map<String, String>> history, StreamCallback callback) {
        try {
            List<Map<String, String>> messages = buildMessages(userId, userMessage, history);
            callSparkWebSocket(messages, callback);
        } catch (Exception e) {
            log.error("Spark stream chat error", e);
            callback.onError("抱歉，我现在无法回答你的问题，请稍后再试。");
        }
    }

    private void callSparkWebSocket(List<Map<String, String>> messages, StreamCallback callback) throws Exception {
        String authUrl = getAuthUrl(getHost(), getPath(), sparkConfig.getApiKey(), sparkConfig.getApiSecret());
        String url = authUrl.replace("https://", "wss://").replace("http://", "ws://");

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder().url(url).build();
        client.newWebSocket(request, new WebSocketListener() {
            private boolean completed = false;

            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                try {
                    webSocket.send(buildSparkRequest(messages).toString());
                } catch (Exception e) {
                    log.error("Error sending Spark request", e);
                    completeWithError(webSocket, "发送消息失败");
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                try {
                    JsonNode jsonNode = objectMapper.readTree(text);
                    JsonNode header = jsonNode.path("header");
                    int code = header.path("code").asInt(0);
                    if (code != 0) {
                        String message = header.path("message").asText("AI服务返回错误");
                        log.error("Spark API error: code={}, message={}", code, message);
                        completeWithError(webSocket, "AI服务错误: " + message);
                        return;
                    }

                    JsonNode choices = jsonNode.path("payload").path("choices");
                    JsonNode textArray = choices.path("text");
                    if (textArray.isArray()) {
                        for (JsonNode textNode : textArray) {
                            String content = textNode.path("content").asText("");
                            if (!content.isEmpty()) {
                                callback.onMessage(content);
                            }
                        }
                    }

                    int status = choices.path("status").asInt(0);
                    if (status == 2) {
                        completed = true;
                        callback.onComplete();
                        webSocket.close(1000, "Normal closure");
                    }
                } catch (Exception e) {
                    log.error("Error processing Spark message", e);
                    completeWithError(webSocket, "处理消息失败");
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                if (!completed) {
                    log.error("Spark WebSocket failure", t);
                    callback.onError("连接AI服务失败");
                }
            }

            private void completeWithError(WebSocket webSocket, String error) {
                if (!completed) {
                    completed = true;
                    callback.onError(error);
                    webSocket.close(1000, "Error");
                }
            }
        });
    }

    private ObjectNode buildSparkRequest(List<Map<String, String>> messages) {
        ObjectNode requestJson = objectMapper.createObjectNode();
        ObjectNode header = requestJson.putObject("header");
        header.put("app_id", sparkConfig.getAppId());
        header.put("uid", UUID.randomUUID().toString().replace("-", "").substring(0, 16));

        ObjectNode parameter = requestJson.putObject("parameter");
        ObjectNode chat = parameter.putObject("chat");
        chat.put("domain", getDomain());
        chat.put("temperature", 0.7);
        chat.put("max_tokens", 2048);

        ObjectNode payload = requestJson.putObject("payload");
        ObjectNode message = payload.putObject("message");
        ArrayNode textArray = message.putArray("text");
        for (Map<String, String> msg : messages) {
            ObjectNode textNode = textArray.addObject();
            textNode.put("role", msg.get("role"));
            textNode.put("content", msg.get("content"));
        }
        return requestJson;
    }

    private List<Map<String, String>> buildMessages(Integer userId, String userMessage, List<Map<String, String>> history) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", buildSystemPrompt(userId)));
        if (history != null) {
            for (Map<String, String> item : history) {
                String role = item.get("role");
                String content = item.get("content");
                if (role != null && content != null && !content.isBlank()) {
                    messages.add(Map.of("role", role, "content", content));
                }
            }
        }
        messages.add(Map.of("role", "user", "content", userMessage));
        return messages;
    }

    private String buildSystemPrompt(Integer userId) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一位专业的健康饮食助手，只回答与健康饮食相关的问题。");
        prompt.append("如果用户问其他问题，请礼貌地说明你只能回答健康饮食相关的问题。\n\n");

        HealthInfo healthInfo = healthInfoMapper.selectOne(
                new LambdaQueryWrapper<HealthInfo>().eq(HealthInfo::getUserId, userId)
        );

        if (healthInfo != null) {
            prompt.append("用户健康信息：\n");
            if (healthInfo.getHeight() != null) prompt.append("- 身高：").append(healthInfo.getHeight()).append("cm\n");
            if (healthInfo.getWeight() != null) prompt.append("- 体重：").append(healthInfo.getWeight()).append("kg\n");
            if (healthInfo.getGender() != null) prompt.append("- 性别：").append(healthInfo.getGender()).append("\n");
            if (healthInfo.getGoal() != null) prompt.append("- 目标：").append(healthInfo.getGoal()).append("\n");
        } else {
            prompt.append("用户尚未填写健康信息。\n");
        }

        List<DietRecord> recentRecords = dietRecordMapper.selectList(
                new LambdaQueryWrapper<DietRecord>()
                        .eq(DietRecord::getUserId, userId)
                        .orderByDesc(DietRecord::getRecordDate)
                        .last("LIMIT 10")
        );

        if (!recentRecords.isEmpty()) {
            prompt.append("\n用户最近的饮食记录：\n");
            for (DietRecord record : recentRecords) {
                prompt.append("- ").append(record.getRecordDate()).append(" ")
                        .append(record.getMealType()).append(": ")
                        .append(record.getRecipeName());
                if (record.getCalories() != null) {
                    prompt.append(" (").append(record.getCalories()).append("千卡/份, 份数")
                            .append(record.getPortion() != null ? record.getPortion() : 1.0)
                            .append(")");
                }
                prompt.append("\n");
            }
        } else {
            prompt.append("\n用户暂无饮食记录。\n");
        }

        prompt.append("\n请根据以上用户信息，提供个性化的健康饮食建议。");
        return prompt.toString();
    }

    private String getAuthUrl(String host, String path, String apiKey, String apiSecret) throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(new Date());

        String signatureOrigin = "host: " + host + "\n" +
                "date: " + date + "\n" +
                "GET " + path + " HTTP/1.1";

        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(spec);
        String signature = Base64.getEncoder().encodeToString(mac.doFinal(signatureOrigin.getBytes(StandardCharsets.UTF_8)));

        String authorizationOrigin = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"",
                apiKey, "hmac-sha256", "host date request-line", signature);
        String authorization = Base64.getEncoder().encodeToString(authorizationOrigin.getBytes(StandardCharsets.UTF_8));

        return String.format("https://%s%s?authorization=%s&date=%s&host=%s",
                host,
                path,
                URLEncoder.encode(authorization, StandardCharsets.UTF_8),
                URLEncoder.encode(date, StandardCharsets.UTF_8),
                host);
    }

    private String getHost() {
        return isBlank(sparkConfig.getHost()) ? DEFAULT_HOST : sparkConfig.getHost();
    }

    private String getPath() {
        return isBlank(sparkConfig.getPath()) ? DEFAULT_PATH : sparkConfig.getPath();
    }

    private String getDomain() {
        return isBlank(sparkConfig.getDomain()) ? DEFAULT_DOMAIN : sparkConfig.getDomain();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
