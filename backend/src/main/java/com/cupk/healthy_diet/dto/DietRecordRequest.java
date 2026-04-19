package com.cupk.healthy_diet.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DietRecordRequest {
    @NotNull(message = "用户ID不能为空")
    private Integer userId;

    private Integer recipeId;

    @NotNull(message = "食谱名称不能为空")
    private String recipeName;

    @NotNull(message = "餐次类型不能为空")
    private String mealType;

    @NotNull(message = "份数不能为空")
    private Double portion;

    @NotNull(message = "热量不能为空")
    private Integer calories;

    @NotNull(message = "蛋白质不能为空")
    private Double protein;

    @NotNull(message = "碳水化合物不能为空")
    private Double carbs;

    @NotNull(message = "脂肪不能为空")
    private Double fat;

    @NotNull(message = "记录日期不能为空")
    private String recordDate;
}
