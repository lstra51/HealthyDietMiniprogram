package com.cupk.healthy_diet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("recipe_suitable_goals")
@Data
public class RecipeSuitableGoal {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer recipeId;

    private String goal;
}
