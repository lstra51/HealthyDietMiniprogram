package com.cupk.healthy_diet.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserBehaviorRequest {
    private Integer userId;

    @NotNull(message = "食谱ID不能为空")
    private Integer recipeId;

    @Pattern(regexp = "view|click|like|cook", message = "行为类型不正确")
    private String behaviorType;
}
