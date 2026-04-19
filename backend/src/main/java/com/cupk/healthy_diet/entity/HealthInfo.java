package com.cupk.healthy_diet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("health_info")
@Data
public class HealthInfo {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private Double height;

    private Double weight;

    private String gender;

    private String goal;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
