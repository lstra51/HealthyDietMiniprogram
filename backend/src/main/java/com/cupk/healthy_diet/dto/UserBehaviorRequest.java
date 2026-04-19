package com.cupk.healthy_diet.dto;

import lombok.Data;

@Data
public class UserBehaviorRequest {
    private Integer userId;
    private Integer recipeId;
    private String behaviorType;
}
