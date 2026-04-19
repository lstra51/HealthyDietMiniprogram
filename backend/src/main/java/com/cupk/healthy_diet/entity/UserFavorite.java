package com.cupk.healthy_diet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("user_favorites")
@Data
public class UserFavorite {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private Integer recipeId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
