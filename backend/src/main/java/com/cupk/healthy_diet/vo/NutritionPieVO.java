package com.cupk.healthy_diet.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionPieVO {
    private String name;
    private Double value;
    private Double percentage;
}
