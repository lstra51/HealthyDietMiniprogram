package com.cupk.healthy_diet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cupk.healthy_diet.entity.*;
import com.cupk.healthy_diet.service.*;
import com.cupk.healthy_diet.util.RecommendationAlgorithm;
import com.cupk.healthy_diet.vo.RecommendationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private static final double RULE_WEIGHT = 0.4;
    private static final double CONTENT_WEIGHT = 0.3;
    private static final double CF_WEIGHT = 0.3;
    private static final int TOP_N = 10;

    private final HealthInfoService healthInfoService;
    private final RecipeService recipeService;
    private final RecommendationAlgorithm recommendationAlgorithm;
    private final com.cupk.healthy_diet.mapper.RecipeMapper recipeMapper;
    private final UserBehaviorService userBehaviorService;
    private final RuleRecommendService ruleRecommendService;
    private final ContentRecommendService contentRecommendService;
    private final CFRecommendService cfRecommendService;
    private final RecommendationValidator recommendationValidator;

    @Override
    public List<RecommendationVO> getPersonalizedRecommendations(Integer userId) {
        HealthInfo healthInfo = healthInfoService.getHealthInfoByUserId(userId);
        boolean isNewUser = userBehaviorService.isNewUser(userId);

        // 先计算各算法的推荐结果
        List<RecipeScore> ruleScores = ruleRecommendService.recommend(healthInfo);
        List<RecipeScore> contentScores = contentRecommendService.recommend(healthInfo);
        List<RecipeScore> cfScores = isNewUser ? new ArrayList<>() : cfRecommendService.recommend(userId);
        
        // 将结果传递给验证器进行日志输出
        recommendationValidator.validateAndLog(userId, ruleScores, contentScores, cfScores);

        Map<Integer, ScoreBreakdown> breakdownMap = new HashMap<>();
        List<RecipeScore> finalScores;

        if (isNewUser) {
            mergeBreakdown(breakdownMap, ruleScores, "rule");
            mergeBreakdown(breakdownMap, contentScores, "content");
            finalScores = handleColdStart(healthInfo, ruleScores, contentScores, breakdownMap);
        } else {
            mergeBreakdown(breakdownMap, ruleScores, "rule");
            mergeBreakdown(breakdownMap, contentScores, "content");
            mergeBreakdown(breakdownMap, cfScores, "cf");
            finalScores = hybridRecommend(ruleScores, contentScores, cfScores);
        }

        List<RecommendationVO> recommendations = convertToVO(finalScores, healthInfo, breakdownMap);

        List<Integer> recipeIds = recommendations.stream()
                .map(RecommendationVO::getRecipeId)
                .collect(java.util.stream.Collectors.toList());
        log.info("推荐完成 - userId: {}, 食谱数量: {}, 食谱ID: {}", userId, recommendations.size(), recipeIds);

        return recommendations;
    }

    private List<RecipeScore> handleColdStart(HealthInfo healthInfo, List<RecipeScore> ruleScores, List<RecipeScore> contentScores, Map<Integer, ScoreBreakdown> breakdownMap) {
        List<RecipeScore> hotScores = getHotRecommendations();
        mergeBreakdown(breakdownMap, hotScores, "cf");

        Map<Integer, Double> scoreMap = new HashMap<>();

        mergeScores(scoreMap, ruleScores, 0.4);
        mergeScores(scoreMap, contentScores, 0.3);
        mergeScores(scoreMap, hotScores, 0.3);

        return buildRecipeScores(scoreMap);
    }

    private List<RecipeScore> hybridRecommend(List<RecipeScore> ruleScores, List<RecipeScore> contentScores, List<RecipeScore> cfScores) {
        Map<Integer, Double> scoreMap = new HashMap<>();

        mergeScores(scoreMap, ruleScores, RULE_WEIGHT);
        mergeScores(scoreMap, contentScores, CONTENT_WEIGHT);
        mergeScores(scoreMap, cfScores, CF_WEIGHT);

        return buildRecipeScores(scoreMap);
    }

    private List<RecipeScore> getHotRecommendations() {
        List<Map<String, Object>> hotRecipes = userBehaviorService.getHotRecipes();
        List<RecipeScore> result = new ArrayList<>();

        for (Map<String, Object> hotRecipe : hotRecipes) {
            Integer recipeId = ((Number) hotRecipe.get("recipe_id")).intValue();
            Recipe recipe = recipeMapper.selectById(recipeId);
            if (recipe != null) {
                result.add(new RecipeScore(recipe, 1.0));
            }
        }

        if (result.isEmpty()) {
            List<Recipe> recipes = recipeMapper.selectList(new LambdaQueryWrapper<Recipe>().last("LIMIT 20"));
            for (Recipe recipe : recipes) {
                result.add(new RecipeScore(recipe, 1.0));
            }
        }

        return result;
    }

    private void mergeScores(Map<Integer, Double> scoreMap, List<RecipeScore> scores, double weight) {
        for (RecipeScore rs : scores) {
            Integer recipeId = rs.getRecipe().getId();
            double currentScore = scoreMap.getOrDefault(recipeId, 0.0);
            scoreMap.put(recipeId, currentScore + rs.getScore() * weight);
        }
    }

    private List<RecipeScore> buildRecipeScores(Map<Integer, Double> scoreMap) {
        if (scoreMap.isEmpty()) {
            return new ArrayList<>();
        }
        List<Integer> ids = new ArrayList<>(scoreMap.keySet());
        Map<Integer, Recipe> recipes = recipeMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(Recipe::getId, recipe -> recipe));

        return scoreMap.entrySet().stream()
                .map(entry -> recipes.containsKey(entry.getKey())
                        ? new RecipeScore(recipes.get(entry.getKey()), entry.getValue())
                        : null)
                .filter(Objects::nonNull)
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .limit(TOP_N)
                .collect(Collectors.toList());
    }

    private List<RecommendationVO> convertToVO(List<RecipeScore> scores, HealthInfo healthInfo, Map<Integer, ScoreBreakdown> breakdownMap) {
        List<RecommendationVO> result = new ArrayList<>();
        double bmi = healthInfo != null && healthInfo.getHeight() != null && healthInfo.getWeight() != null
                ? healthInfo.getWeight() / ((healthInfo.getHeight() / 100.0) * (healthInfo.getHeight() / 100.0))
                : 0;

        for (RecipeScore rs : scores) {
            Recipe recipe = rs.getRecipe();
            RecommendationVO vo = new RecommendationVO();
            vo.setRecipeId(recipe.getId());
            vo.setName(recipe.getName() != null ? recipe.getName() : "未知食谱");
            vo.setCategory(recipe.getCategory() != null ? recipe.getCategory() : "其他");
            vo.setImage(recipe.getImage());
            vo.setCalories(recipe.getCalories());
            vo.setProtein(recipe.getProtein());
            vo.setCarbs(recipe.getCarbs());
            vo.setFat(recipe.getFat());
            vo.setScore(Math.round(rs.getScore() * 100.0) / 100.0);
            ScoreBreakdown breakdown = breakdownMap.getOrDefault(recipe.getId(), new ScoreBreakdown());
            vo.setRuleScore(round(breakdown.ruleScore));
            vo.setContentScore(round(breakdown.contentScore));
            vo.setCollaborativeScore(round(breakdown.collaborativeScore));
            vo.setReason(generateReason(recipe, healthInfo, bmi, breakdown));
            result.add(vo);
        }

        return result;
    }

    private String generateReason(Recipe recipe, HealthInfo healthInfo, double bmi, ScoreBreakdown breakdown) {
        if (healthInfo != null && healthInfo.getGoal() != null) {
            if ("减脂".equals(healthInfo.getGoal()) && recipe.getCalories() != null && recipe.getCalories() <= 500) {
                return "低热量食谱，匹配您的减脂目标";
            }
            if ("增肌".equals(healthInfo.getGoal()) && recipe.getProtein() != null && recipe.getProtein() >= 20) {
                return "高蛋白食谱，适合您的增肌目标";
            }
            if (breakdown.collaborativeScore > 0) {
                return "结合您的浏览和收藏行为推荐";
            }
            return String.format("根据您的 BMI(%.1f) 和%s目标推荐", bmi, healthInfo.getGoal());
        }
        return "热门推荐";
    }

    private void mergeBreakdown(Map<Integer, ScoreBreakdown> breakdownMap, List<RecipeScore> scores, String source) {
        for (RecipeScore score : scores) {
            ScoreBreakdown breakdown = breakdownMap.computeIfAbsent(score.getRecipe().getId(), id -> new ScoreBreakdown());
            switch (source) {
                case "rule" -> breakdown.ruleScore = Math.max(breakdown.ruleScore, score.getScore());
                case "content" -> breakdown.contentScore = Math.max(breakdown.contentScore, score.getScore());
                case "cf" -> breakdown.collaborativeScore = Math.max(breakdown.collaborativeScore, score.getScore());
                default -> {
                }
            }
        }
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static class ScoreBreakdown {
        double ruleScore = 0.0;
        double contentScore = 0.0;
        double collaborativeScore = 0.0;
    }
}
