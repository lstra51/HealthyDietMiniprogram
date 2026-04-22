package com.cupk.healthy_diet.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionTrendVO {
    private List<String> dates;
    private List<Integer> calories;
    private List<Double> protein;
    private List<Double> carbs;
    private List<Double> fat;
}
