package com.cupk.healthy_diet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("recipes")
@Data
public class Recipe {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String name;

    private String category;

    private String image;

    private String description;

    private Integer calories;

    private Double protein;

    private Double carbs;

    private Double fat;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
