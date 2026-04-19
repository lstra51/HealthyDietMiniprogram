package com.cupk.healthy_diet.util;

import com.cupk.healthy_diet.entity.HealthInfo;
import com.cupk.healthy_diet.entity.Recipe;
import com.cupk.healthy_diet.service.RecipeService;
import com.cupk.healthy_diet.vo.RecommendationVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RecommendationAlgorithm {

    private static final double W1 = 0.4; // 蛋白质权重
    private static final double W2 = 0.3; // 热量匹配权重
    private static final double W3 = 0.2; // 脂肪控制权重
    private static final double W4 = 0.1; // 目标匹配度权重

    public List<RecommendationVO> getRecommendations(HealthInfo healthInfo, List<Recipe> recipes, RecipeService recipeService) {
        if (recipes == null || recipes.isEmpty()) {
            return new ArrayList<>();
        }
        
        if (healthInfo == null) {
            return getDefaultRecommendations(recipes);
        }

        Double height = healthInfo.getHeight();
        Double weight = healthInfo.getWeight();
        String goal = healthInfo.getGoal();
        
        if (height == null || weight == null || goal == null) {
            return getDefaultRecommendations(recipes);
        }

        double bmi = calculateBMI(height, weight);

        List<RecommendationVO> scoredRecipes = new ArrayList<>();

        for (Recipe recipe : recipes) {
            if (recipe == null) continue;
            
            List<String> suitableGoals = recipeService.getRecipeSuitableGoals(recipe.getId());
            double score = calculateScore(recipe, goal, bmi);
            boolean isSuitable = suitableGoals != null && suitableGoals.contains(goal);

            RecommendationVO vo = new RecommendationVO();
            vo.setRecipeId(recipe.getId());
            vo.setName(recipe.getName() != null ? recipe.getName() : "未知食谱");
            vo.setCategory(recipe.getCategory() != null ? recipe.getCategory() : "其他");
            vo.setImage(recipe.getImage());
            vo.setCalories(recipe.getCalories());
            vo.setProtein(recipe.getProtein());
            vo.setCarbs(recipe.getCarbs());
            vo.setFat(recipe.getFat());
            vo.setScore(Math.round(score * 100.0) / 100.0);
            vo.setReason(generateReason(goal, bmi, isSuitable));

            scoredRecipes.add(vo);
        }

        return scoredRecipes.stream()
                .sorted(Comparator.comparingDouble(RecommendationVO::getScore).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    private double calculateScore(Recipe recipe, String goal, double bmi) {
        double proteinScore = calculateProteinScore(recipe.getProtein(), goal);
        double calorieScore = calculateCalorieScore(recipe.getCalories(), goal);
        double fatScore = calculateFatScore(recipe.getFat(), goal);
        double goalScore = calculateGoalScore(recipe, goal);

        return W1 * proteinScore + W2 * calorieScore + W3 * fatScore + W4 * goalScore;
    }

    private double calculateProteinScore(double protein, String goal) {
        switch (goal) {
            case "减脂":
                return protein >= 20 && protein <= 40 ? 1.0 :
                       protein > 40 ? 0.8 : 0.5;
            case "增肌":
                return protein >= 30 ? 1.0 : protein >= 20 ? 0.8 : 0.5;
            case "保持":
                return protein >= 15 && protein <= 35 ? 1.0 : 0.7;
            default:
                return 0.5;
        }
    }

    private double calculateCalorieScore(int calories, String goal) {
        switch (goal) {
            case "减脂":
                return calories <= 300 ? 1.0 : calories <= 500 ? 0.7 : 0.4;
            case "增肌":
                return calories >= 400 && calories <= 700 ? 1.0 : calories > 700 ? 0.8 : 0.6;
            case "保持":
                return calories >= 300 && calories <= 600 ? 1.0 : 0.7;
            default:
                return 0.5;
        }
    }

    private double calculateFatScore(double fat, String goal) {
        switch (goal) {
            case "减脂":
                return fat <= 15 ? 1.0 : fat <= 25 ? 0.7 : 0.4;
            case "增肌":
                return fat >= 10 && fat <= 30 ? 1.0 : 0.7;
            case "保持":
                return fat >= 8 && fat <= 25 ? 1.0 : 0.7;
            default:
                return 0.5;
        }
    }

    private double calculateGoalScore(Recipe recipe, String goal) {
        return 1.0;
    }

    private String generateReason(String goal, double bmi, boolean isSuitable) {
        String bmiStr = String.format("%.1f", bmi);
        return String.format("根据您的 BMI(%s) 和%s目标推荐", bmiStr, goal);
    }

    private List<RecommendationVO> getDefaultRecommendations(List<Recipe> recipes) {
        return recipes.stream()
                .limit(5)
                .map(recipe -> {
                    RecommendationVO vo = new RecommendationVO();
                    vo.setRecipeId(recipe.getId());
                    vo.setName(recipe.getName());
                    vo.setCategory(recipe.getCategory());
                    vo.setImage(recipe.getImage());
                    vo.setCalories(recipe.getCalories());
                    vo.setProtein(recipe.getProtein());
                    vo.setCarbs(recipe.getCarbs());
                    vo.setFat(recipe.getFat());
                    vo.setScore(1.0);
                    vo.setReason("热门推荐");
                    return vo;
                })
                .collect(Collectors.toList());
    }

    private double calculateBMI(double height, double weight) {
        if (height <= 0) return 0;
        double heightInMeters = height / 100.0;
        return weight / (heightInMeters * heightInMeters);
    }
}
