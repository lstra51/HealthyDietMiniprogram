package com.cupk.healthy_diet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cupk.healthy_diet.entity.HealthInfo;
import com.cupk.healthy_diet.entity.Recipe;
import com.cupk.healthy_diet.entity.RecipeScore;
import com.cupk.healthy_diet.entity.RecipeSuitableGoal;
import com.cupk.healthy_diet.entity.RecipeTag;
import com.cupk.healthy_diet.mapper.RecipeMapper;
import com.cupk.healthy_diet.mapper.RecipeSuitableGoalMapper;
import com.cupk.healthy_diet.mapper.RecipeTagMapper;
import com.cupk.healthy_diet.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContentRecommendService {

    private final RecipeMapper recipeMapper;
    private final RecipeTagMapper recipeTagMapper;
    private final RecipeSuitableGoalMapper recipeSuitableGoalMapper;
    private final RecipeService recipeService;

    public List<RecipeScore> recommend(HealthInfo healthInfo) {
        List<Recipe> recipes = recipeMapper.selectList(new LambdaQueryWrapper<Recipe>().last("LIMIT 100"));

        Set<String> userFeatures = extractUserFeatures(healthInfo);

        List<RecipeScore> result = new ArrayList<>();

        for (Recipe recipe : recipes) {
            Set<String> recipeFeatures = extractRecipeFeatures(recipe);
            double score = calcSimilarity(userFeatures, recipeFeatures);
            result.add(new RecipeScore(recipe, score));
        }

        return result.stream()
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .collect(Collectors.toList());
    }// 根据用户信息提取特征

    private Set<String> extractUserFeatures(HealthInfo healthInfo) {
        Set<String> features = new HashSet<>();
        if (healthInfo != null && healthInfo.getGoal() != null) {
            features.add(healthInfo.getGoal());
        }
        return features;
    }// 根据食谱信息提取特征

    private Set<String> extractRecipeFeatures(Recipe recipe) {
        Set<String> features = new HashSet<>();

        List<RecipeTag> tags = recipeTagMapper.selectList(
                new LambdaQueryWrapper<RecipeTag>().eq(RecipeTag::getRecipeId, recipe.getId())
        );
        for (RecipeTag tag : tags) {
            features.add(tag.getTag());
        }

        List<RecipeSuitableGoal> goals = recipeSuitableGoalMapper.selectList(
                new LambdaQueryWrapper<RecipeSuitableGoal>().eq(RecipeSuitableGoal::getRecipeId, recipe.getId())
        );
        for (RecipeSuitableGoal goal : goals) {
            features.add(goal.getGoal());
        }

        if (recipe.getCategory() != null) {
            features.add(recipe.getCategory());
        }

        return features;
    }// 计算相似度

    private double calcSimilarity(Set<String> userTags, Set<String> recipeTags) {
        if (userTags.isEmpty() || recipeTags.isEmpty()) {
            return 0.0;
        }

        Set<String> intersection = new HashSet<>(userTags);
        intersection.retainAll(recipeTags);

        Set<String> union = new HashSet<>(userTags);
        union.addAll(recipeTags);

        return (double) intersection.size() / union.size();
    }// 获取推荐结果
}
