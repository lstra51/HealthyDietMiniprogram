package com.cupk.healthy_diet.dto;

import lombok.Data;

@Data
public class UserFavoriteRequest {
    private Integer userId;
    private Integer recipeId;
}
