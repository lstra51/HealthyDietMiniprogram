package com.cupk.healthy_diet.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateRecipeDTO {
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
    private List<String> suitableGoals;
    private List<String> steps;
}
