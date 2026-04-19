package com.cupk.healthy_diet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("recipe_steps")
@Data
public class RecipeStep {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer recipeId;

    private Integer stepNumber;

    private String description;
}
