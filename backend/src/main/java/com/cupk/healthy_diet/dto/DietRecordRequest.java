package com.cupk.healthy_diet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class DietRecordRequest {
    private Integer userId;

    private Integer recipeId;

    @NotNull(message = "食谱名称不能为空")
    private String recipeName;

    @NotNull(message = "餐次类型不能为空")
    @Pattern(regexp = "早餐|午餐|晚餐|加餐", message = "餐次类型不正确")
    private String mealType;

    @NotNull(message = "份数不能为空")
    @DecimalMin(value = "0.1", message = "份数必须大于0")
    private Double portion;

    @NotNull(message = "热量不能为空")
    @Positive(message = "热量必须大于0")
    private Integer calories;

    @NotNull(message = "蛋白质不能为空")
    @DecimalMin(value = "0.0", message = "蛋白质不能为负数")
    private Double protein;

    @NotNull(message = "碳水化合物不能为空")
    @DecimalMin(value = "0.0", message = "碳水化合物不能为负数")
    private Double carbs;

    @NotNull(message = "脂肪不能为空")
    @DecimalMin(value = "0.0", message = "脂肪不能为负数")
    private Double fat;

    @NotNull(message = "记录日期不能为空")
    private String recordDate;
}
