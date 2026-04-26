package com.cupk.healthy_diet.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyNutritionVO {
    private Integer totalCalories;
    private Double totalProtein;
    private Double totalCarbs;
    private Double totalFat;
    private Integer targetCalories;
    private Double targetProtein;
    private Double targetCarbs;
    private Double targetFat;
    private Double caloriesProgress;
    private Double proteinProgress;
    private Double carbsProgress;
    private Double fatProgress;
}
