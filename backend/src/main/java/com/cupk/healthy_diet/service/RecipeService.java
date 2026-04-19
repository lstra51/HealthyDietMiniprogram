package com.cupk.healthy_diet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cupk.healthy_diet.entity.Recipe;
import com.cupk.healthy_diet.vo.RecipeDetailVO;
import com.cupk.healthy_diet.vo.RecipeVO;

import java.util.List;

public interface RecipeService extends IService<Recipe> {
    List<RecipeVO> getRecipeList(String category, String keyword);
    RecipeDetailVO getRecipeDetail(Integer id);
    List<String> getRecipeIngredients(Integer recipeId);
    List<String> getRecipeTags(Integer recipeId);
    List<String> getRecipeSteps(Integer recipeId);
    List<String> getRecipeSuitableGoals(Integer recipeId);
}
