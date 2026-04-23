package com.cupk.healthy_diet.vo;

import lombok.Data;

import java.util.List;

@Data
public class DishRecognitionVO {
    private String bestName;
    private String bestCalories;
    private Double bestProbability;
    private List<DishItemVO> items;

    @Data
    public static class DishItemVO {
        private String name;
        private String calorie;
        private Double probability;
    }
}
