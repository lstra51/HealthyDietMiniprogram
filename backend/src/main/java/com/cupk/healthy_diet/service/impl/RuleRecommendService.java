package com.cupk.healthy_diet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cupk.healthy_diet.entity.HealthInfo;
import com.cupk.healthy_diet.entity.Recipe;
import com.cupk.healthy_diet.entity.RecipeScore;
import com.cupk.healthy_diet.mapper.RecipeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RuleRecommendService {

    private final RecipeMapper recipeMapper;

    public List<RecipeScore> recommend(HealthInfo healthInfo) {
        List<Recipe> recipes;

        if (healthInfo == null || healthInfo.getGoal() == null) {
            recipes = recipeMapper.selectList(new LambdaQueryWrapper<Recipe>().last("LIMIT 50"));
        } else {
            String goal = healthInfo.getGoal();
            LambdaQueryWrapper<Recipe> wrapper = new LambdaQueryWrapper<>();

            switch (goal) {
                case "减脂":
                    wrapper.le(Recipe::getCalories, 500);// 卡路里低于500
                    break;
                case "增肌":
                    wrapper.ge(Recipe::getProtein, 20);// 蛋白质高于20
                    break;
                default:
                    break;
            }

            wrapper.last("LIMIT 50");
            recipes = recipeMapper.selectList(wrapper);

            if (recipes.isEmpty()) {
                recipes = recipeMapper.selectList(new LambdaQueryWrapper<Recipe>().last("LIMIT 50"));
            }
        }

        return recipes.stream()
                .map(recipe -> new RecipeScore(recipe, 1.0))
                .collect(Collectors.toList());
    }
}
