package com.cupk.healthy_diet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cupk.healthy_diet.entity.*;
import com.cupk.healthy_diet.exception.BusinessException;
import com.cupk.healthy_diet.mapper.*;
import com.cupk.healthy_diet.service.RecipeService;
import com.cupk.healthy_diet.vo.RecipeDetailVO;
import com.cupk.healthy_diet.vo.RecipeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeServiceImpl extends ServiceImpl<RecipeMapper, Recipe> implements RecipeService {

    private final RecipeIngredientMapper recipeIngredientMapper;
    private final RecipeTagMapper recipeTagMapper;
    private final RecipeStepMapper recipeStepMapper;
    private final RecipeSuitableGoalMapper recipeSuitableGoalMapper;

    @Override
    public List<RecipeVO> getRecipeList(String category, String keyword) {
        LambdaQueryWrapper<Recipe> wrapper = new LambdaQueryWrapper<>();
        
        if (category != null && !category.isEmpty() && !category.equals("全部")) {
            wrapper.eq(Recipe::getCategory, category);
        }
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Recipe::getName, keyword)
                    .or()
                    .like(Recipe::getDescription, keyword));
        }

        List<Recipe> recipes = this.list(wrapper);
        
        return recipes.stream().map(recipe -> {
            List<String> tags = getRecipeTags(recipe.getId());
            return new RecipeVO(
                recipe.getId(),
                recipe.getName(),
                recipe.getCategory(),
                recipe.getImage(),
                recipe.getCalories(),
                tags
            );
        }).collect(Collectors.toList());
    }

    @Override
    public RecipeDetailVO getRecipeDetail(Integer id) {
        Recipe recipe = this.getById(id);
        if (recipe == null) {
            throw new BusinessException("食谱不存在");
        }

        List<String> ingredients = getRecipeIngredients(id);
        List<String> tags = getRecipeTags(id);
        List<String> steps = getRecipeSteps(id);
        List<String> suitableGoals = getRecipeSuitableGoals(id);

        return new RecipeDetailVO(
            recipe.getId(),
            recipe.getName(),
            recipe.getCategory(),
            recipe.getImage(),
            recipe.getDescription(),
            recipe.getCalories(),
            recipe.getProtein(),
            recipe.getCarbs(),
            recipe.getFat(),
            ingredients,
            tags,
            steps,
            suitableGoals
        );
    }

    @Override
    public List<String> getRecipeIngredients(Integer recipeId) {
        LambdaQueryWrapper<RecipeIngredient> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecipeIngredient::getRecipeId, recipeId);
        wrapper.orderByAsc(RecipeIngredient::getSortOrder);
        return recipeIngredientMapper.selectList(wrapper).stream()
                .map(RecipeIngredient::getIngredient)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getRecipeTags(Integer recipeId) {
        LambdaQueryWrapper<RecipeTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecipeTag::getRecipeId, recipeId);
        return recipeTagMapper.selectList(wrapper).stream()
                .map(RecipeTag::getTag)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getRecipeSteps(Integer recipeId) {
        LambdaQueryWrapper<RecipeStep> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecipeStep::getRecipeId, recipeId);
        wrapper.orderByAsc(RecipeStep::getStepNumber);
        return recipeStepMapper.selectList(wrapper).stream()
                .map(RecipeStep::getDescription)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getRecipeSuitableGoals(Integer recipeId) {
        LambdaQueryWrapper<RecipeSuitableGoal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecipeSuitableGoal::getRecipeId, recipeId);
        return recipeSuitableGoalMapper.selectList(wrapper).stream()
                .map(RecipeSuitableGoal::getGoal)
                .collect(Collectors.toList());
    }
}
