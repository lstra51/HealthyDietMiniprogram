package com.cupk.healthy_diet.service;

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
import okhttp3.*;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SparkChatService {

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
        try {
            String systemPrompt = buildSystemPrompt(userId);
            
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            
            if (history != null) {
                messages.addAll(history);
            }
            
            messages.add(Map.of("role", "user", "content", userMessage));

            return callSparkApi(messages);
        } catch (Exception e) {
            log.error("Spark chat error", e);
            return "抱歉，我现在无法回答你的问题，请稍后再试。";
        }
    }

    public void streamChat(Integer userId, String userMessage, List<Map<String, String>> history, StreamCallback callback) {
        try {
            String systemPrompt = buildSystemPrompt(userId);
            
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            
            if (history != null) {
                messages.addAll(history);
            }
            
            messages.add(Map.of("role", "user", "content", userMessage));

            callSparkApiStream(messages, callback);
        } catch (Exception e) {
            log.error("Spark stream chat error", e);
            callback.onError("抱歉，我现在无法回答你的问题，请稍后再试。");
        }
    }

    private String buildSystemPrompt(Integer userId) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("你是一位专业的健康饮食助手，只回答与健康饮食相关的问题。");
        prompt.append("如果用户问其他问题，请礼貌地说明你只能回答健康饮食相关的问题。\n\n");
        
        HealthInfo healthInfo = healthInfoMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<HealthInfo>()
                .eq(HealthInfo::getUserId, userId)
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
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DietRecord>()
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
                    prompt.append(" (").append(record.getCalories()).append("千卡)");
                }
                prompt.append("\n");
            }
        } else {
            prompt.append("\n用户暂无饮食记录。\n");
        }
        
        prompt.append("\n请根据以上用户信息，提供个性化的健康饮食建议。");
        
        return prompt.toString();
    }

    private String callSparkApi(List<Map<String, String>> messages) throws Exception {
        String authUrl = getAuthUrl(sparkConfig.getHost(), sparkConfig.getPath(), sparkConfig.getApiKey(), sparkConfig.getApiSecret());
        String url = authUrl.replace("http://", "ws://").replace("https://", "wss://");
        
        OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .build();
        
        final CountDownLatch latch = new CountDownLatch(1);
        final StringBuilder responseBuilder = new StringBuilder();
        
        Request request = new Request.Builder().url(url).build();
        WebSocket webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                try {
                    ObjectNode requestJson = objectMapper.createObjectNode();
                    ObjectNode header = requestJson.putObject("header");
                    header.put("app_id", sparkConfig.getAppId());
                    header.put("uid", UUID.randomUUID().toString().substring(0, 16));
                    
                    ObjectNode parameter = requestJson.putObject("parameter");
                    ObjectNode chat = parameter.putObject("chat");
                    chat.put("domain", sparkConfig.getDomain());
                    chat.put("temperature", 0.7);
                    chat.put("max_tokens", 2048);
                    
                    ObjectNode payload = requestJson.putObject("payload");
                    ObjectNode message = payload.putObject("message");
                    ArrayNode textArray = message.putArray("text");
                    
                    for (Map<String, String> msg : messages) {
                        ObjectNode textNode = objectMapper.createObjectNode();
                        textNode.put("role", msg.get("role"));
                        textNode.put("content", msg.get("content"));
                        textArray.add(textNode);
                    }
                    
                    webSocket.send(requestJson.toString());
                } catch (Exception e) {
                    log.error("Error sending message", e);
                    latch.countDown();
                }
            }
            
            @Override
            public void onMessage(WebSocket webSocket, String text) {
                try {
                    JsonNode jsonNode = objectMapper.readTree(text);
                    JsonNode header = jsonNode.get("header");
                    int code = header.get("code").asInt();
                    
                    if (code != 0) {
                        log.error("Spark API error: code={}, message={}", code, header.get("message").asText());
                        latch.countDown();
                        return;
                    }
                    
                    JsonNode choices = jsonNode.get("payload").get("choices");
                    JsonNode textArray = choices.get("text");
                    for (JsonNode textNode : textArray) {
                        responseBuilder.append(textNode.get("content").asText());
                    }
                    
                    int status = choices.get("status").asInt();
                    if (status == 2) {
                        webSocket.close(1000, "Normal closure");
                        latch.countDown();
                    }
                } catch (Exception e) {
                    log.error("Error processing message", e);
                    latch.countDown();
                }
            }
            
            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                log.error("WebSocket failure", t);
                latch.countDown();
            }
        });
        
        boolean success = latch.await(60, TimeUnit.SECONDS);
        if (!success) {
            webSocket.close(1000, "Timeout");
            throw new RuntimeException("Request timeout");
        }
        
        return responseBuilder.toString();
    }

    private void callSparkApiStream(List<Map<String, String>> messages, StreamCallback callback) throws Exception {
        String authUrl = getAuthUrl(sparkConfig.getHost(), sparkConfig.getPath(), sparkConfig.getApiKey(), sparkConfig.getApiSecret());
        String url = authUrl.replace("http://", "ws://").replace("https://", "wss://");
        
        OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .build();
        
        Request request = new Request.Builder().url(url).build();
        client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                try {
                    ObjectNode requestJson = objectMapper.createObjectNode();
                    ObjectNode header = requestJson.putObject("header");
                    header.put("app_id", sparkConfig.getAppId());
                    header.put("uid", UUID.randomUUID().toString().substring(0, 16));
                    
                    ObjectNode parameter = requestJson.putObject("parameter");
                    ObjectNode chat = parameter.putObject("chat");
                    chat.put("domain", sparkConfig.getDomain());
                    chat.put("temperature", 0.7);
                    chat.put("max_tokens", 2048);
                    
                    ObjectNode payload = requestJson.putObject("payload");
                    ObjectNode message = payload.putObject("message");
                    ArrayNode textArray = message.putArray("text");
                    
                    for (Map<String, String> msg : messages) {
                        ObjectNode textNode = objectMapper.createObjectNode();
                        textNode.put("role", msg.get("role"));
                        textNode.put("content", msg.get("content"));
                        textArray.add(textNode);
                    }
                    
                    webSocket.send(requestJson.toString());
                } catch (Exception e) {
                    log.error("Error sending message", e);
                    callback.onError("发送消息失败");
                }
            }
            
            @Override
            public void onMessage(WebSocket webSocket, String text) {
                try {
                    JsonNode jsonNode = objectMapper.readTree(text);
                    JsonNode header = jsonNode.get("header");
                    int code = header.get("code").asInt();
                    
                    if (code != 0) {
                        log.error("Spark API error: code={}, message={}", code, header.get("message").asText());
                        callback.onError("API错误: " + header.get("message").asText());
                        webSocket.close(1000, "Error");
                        return;
                    }
                    
                    JsonNode choices = jsonNode.get("payload").get("choices");
                    JsonNode textArray = choices.get("text");
                    for (JsonNode textNode : textArray) {
                        String content = textNode.get("content").asText();
                        callback.onMessage(content);
                    }
                    
                    int status = choices.get("status").asInt();
                    if (status == 2) {
                        webSocket.close(1000, "Normal closure");
                        callback.onComplete();
                    }
                } catch (Exception e) {
                    log.error("Error processing message", e);
                    callback.onError("处理消息失败");
                    webSocket.close(1000, "Error");
                }
            }
            
            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                log.error("WebSocket failure", t);
                callback.onError("连接失败");
            }
        });
    }

    private String getAuthUrl(String host, String path, String apiKey, String apiSecret) throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(new Date());
        
        String signatureOrigin = "host: " + host + "\n" +
                "date: " + date + "\n" +
                "GET " + path + " HTTP/1.1";
        
        Mac mac = Mac.getInstance("hmacsha256");
        SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "hmacsha256");
        mac.init(spec);
        byte[] hexDigits = mac.doFinal(signatureOrigin.getBytes(StandardCharsets.UTF_8));
        String signature = Base64.getEncoder().encodeToString(hexDigits);
        
        String authorizationOrigin = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"",
                apiKey, "hmac-sha256", "host date request-line", signature);
        String authorization = Base64.getEncoder().encodeToString(authorizationOrigin.getBytes(StandardCharsets.UTF_8));
        
        return String.format("https://%s%s?authorization=%s&date=%s&host=%s",
                host, path, authorization, java.net.URLEncoder.encode(date, StandardCharsets.UTF_8), host);
    }
}
