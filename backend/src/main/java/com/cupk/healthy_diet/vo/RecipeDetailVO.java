package com.cupk.healthy_diet.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeDetailVO {
    private Integer id;
    private String name;
    private String category;
    private String image;
    private String description;
    private Integer calories;
    private Double protein;
    private Double carbs;
    private Double fat;
    private List<String> ingredients;
    private List<String> tags;
    private List<String> steps;
    private List<String> suitableGoals;
}
