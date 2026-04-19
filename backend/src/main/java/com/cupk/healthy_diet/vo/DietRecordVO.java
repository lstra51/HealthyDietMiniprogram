package com.cupk.healthy_diet.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DietRecordVO {
    private Integer id;
    private Integer recipeId;
    private String recipeName;
    private String mealType;
    private Double portion;
    private Integer calories;
    private Double protein;
    private Double carbs;
    private Double fat;
    private LocalDate recordDate;
}
