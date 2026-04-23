package com.cupk.healthy_diet.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationVO {
    private Integer recipeId;
    private String name;
    private String category;
    private String image;
    private Integer calories;
    private Double protein;
    private Double carbs;
    private Double fat;
    private Double score;
    private String reason;
    private Double ruleScore;
    private Double contentScore;
    private Double collaborativeScore;
}
