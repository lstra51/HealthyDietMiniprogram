package com.cupk.healthy_diet.controller;

import com.cupk.healthy_diet.common.Result;
import com.cupk.healthy_diet.dto.ChatRequest;
import com.cupk.healthy_diet.security.AuthContext;
import com.cupk.healthy_diet.service.SparkChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIAssistantController {

    private final SparkChatService sparkChatService;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @PostMapping("/chat")
    public Result<String> chat(@RequestBody ChatRequest request,
                               @RequestAttribute(AuthContext.USER_ID) Integer userId) {
        String response = sparkChatService.chat(
            userId,
            request.getMessage(),
            (List<Map<String, String>>) (List<?>) request.getHistory()
        );
        return Result.success(response);
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestBody ChatRequest request,
                                 @RequestAttribute(AuthContext.USER_ID) Integer userId) {
        SseEmitter emitter = new SseEmitter(60000L);
        
        executor.execute(() -> {
            try {
                sparkChatService.streamChat(
                    userId,
                    request.getMessage(),
                    (List<Map<String, String>>) (List<?>) request.getHistory(),
                    new SparkChatService.StreamCallback() {
                        @Override
                        public void onMessage(String content) {
                            try {
                                emitter.send(SseEmitter.event().data(content));
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        }

                        @Override
                        public void onComplete() {
                            emitter.complete();
                        }

                        @Override
                        public void onError(String error) {
                            try {
                                emitter.send(SseEmitter.event().data("[ERROR]" + error));
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                            emitter.complete();
                        }
                    }
                );
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
}
