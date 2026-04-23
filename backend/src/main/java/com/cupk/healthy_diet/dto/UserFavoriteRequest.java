package com.cupk.healthy_diet.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserFavoriteRequest {
    private Integer userId;

    @NotNull(message = "食谱ID不能为空")
    private Integer recipeId;
}
