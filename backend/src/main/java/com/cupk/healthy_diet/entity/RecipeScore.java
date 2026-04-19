package com.cupk.healthy_diet.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeScore {
    private Recipe recipe;
    private Double score;
}
