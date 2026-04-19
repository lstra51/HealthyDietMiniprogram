package com.cupk.healthy_diet.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HealthInfoRequest {
    @NotNull(message = "用户ID不能为空")
    private Integer userId;

    @NotNull(message = "身高不能为空")
    private Double height;

    @NotNull(message = "体重不能为空")
    private Double weight;

    @NotNull(message = "性别不能为空")
    private String gender;

    @NotNull(message = "健康目标不能为空")
    private String goal;
}
