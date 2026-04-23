package com.cupk.healthy_diet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateRecipeDTO {
    @NotBlank(message = "食谱名称不能为空")
    @Size(max = 100, message = "食谱名称不能超过100个字符")
    private String name;

    @NotBlank(message = "分类不能为空")
    private String category;

    private String image;

    @Size(max = 1000, message = "描述不能超过1000个字符")
    private String description;

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

    @NotEmpty(message = "至少添加一个食材")
    private List<String> ingredients;

    private List<String> tags;
    private List<String> suitableGoals;

    @NotEmpty(message = "至少添加一个步骤")
    private List<String> steps;
}
