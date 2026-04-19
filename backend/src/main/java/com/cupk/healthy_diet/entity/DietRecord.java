package com.cupk.healthy_diet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("diet_records")
@Data
public class DietRecord {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private Integer recipeId;

    private String recipeName;

    private String mealType;

    private Double portion;

    private Integer calories;

    private Double protein;

    private Double carbs;

    private Double fat;

    private LocalDate recordDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
