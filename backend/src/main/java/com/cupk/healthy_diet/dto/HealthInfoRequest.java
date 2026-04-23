package com.cupk.healthy_diet.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class HealthInfoRequest {
    private Integer userId;

    @NotNull(message = "身高不能为空")
    @DecimalMin(value = "50.0", message = "身高不能低于50cm")
    @DecimalMax(value = "250.0", message = "身高不能高于250cm")
    private Double height;

    @NotNull(message = "体重不能为空")
    @DecimalMin(value = "20.0", message = "体重不能低于20kg")
    @DecimalMax(value = "300.0", message = "体重不能高于300kg")
    private Double weight;

    @NotNull(message = "性别不能为空")
    @Pattern(regexp = "男|女|其他", message = "性别只能是男、女或其他")
    private String gender;

    @NotNull(message = "健康目标不能为空")
    @Pattern(regexp = "减脂|增肌|保持", message = "健康目标只能是减脂、增肌或保持")
    private String goal;
}
