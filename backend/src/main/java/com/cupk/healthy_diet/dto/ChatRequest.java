package com.cupk.healthy_diet.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ChatRequest {
    private Integer userId;
    private String message;
    private List<Map<String, String>> history;
}
