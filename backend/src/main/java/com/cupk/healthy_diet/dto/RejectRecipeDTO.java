package com.cupk.healthy_diet.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectRecipeDTO {
    @NotBlank(message = "拒绝原因不能为空")
    private String reason;
}
