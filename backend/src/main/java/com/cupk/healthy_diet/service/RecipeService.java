package com.cupk.healthy_diet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cupk.healthy_diet.dto.CreateRecipeDTO;
import com.cupk.healthy_diet.dto.RecipeImportDTO;
import com.cupk.healthy_diet.entity.Recipe;
import com.cupk.healthy_diet.vo.RecipeDetailVO;
import com.cupk.healthy_diet.vo.RecipeVO;

import java.util.List;

public interface RecipeService extends IService<Recipe> {
    List<RecipeVO> getRecipeList(String category, String keyword, Integer minCalories, Integer maxCalories,
                                 Double minProtein, String tag, String goal);
    RecipeDetailVO getRecipeDetail(Integer id);
    List<String> getRecipeIngredients(Integer recipeId);
    List<String> getRecipeTags(Integer recipeId);
    List<String> getRecipeSteps(Integer recipeId);
    List<String> getRecipeSuitableGoals(Integer recipeId);
    List<String> getAvailableTags();
    void importRecipes(List<RecipeImportDTO> recipeDTOs);
    Integer createRecipe(CreateRecipeDTO dto, Integer userId);
    List<RecipeVO> getPendingRecipes();
    List<RecipeVO> getAdminRecipeList(String status, String category, String keyword);
    void approveRecipe(Integer id);
    void rejectRecipe(Integer id, String reason);
    List<RecipeVO> getUserRecipes(Integer userId);
    void updateRecipe(Integer id, CreateRecipeDTO dto, Integer userId);
    void adminUpdateRecipe(Integer id, CreateRecipeDTO dto);
    void adminDeleteRecipe(Integer id);
}
