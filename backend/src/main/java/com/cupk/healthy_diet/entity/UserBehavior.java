package com.cupk.healthy_diet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("user_behavior")
@Data
public class UserBehavior {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer userId;

    private Integer recipeId;

    private String behaviorType;

    private Integer weight;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
