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
public class StatisticsVO {
    private NutritionTrendVO trend;
    private List<NutritionPieVO> pieData;
    private Double avgCalories;
    private Double avgProtein;
    private Double avgCarbs;
    private Double avgFat;
}
