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

        List<RecipeScore> finalScores;

        if (isNewUser) {
            finalScores = handleColdStart(healthInfo, ruleScores, contentScores);
        } else {
            finalScores = hybridRecommend(ruleScores, contentScores, cfScores);
        }

        List<RecommendationVO> recommendations = convertToVO(finalScores, healthInfo);

        List<Integer> recipeIds = recommendations.stream()
                .map(RecommendationVO::getRecipeId)
                .collect(java.util.stream.Collectors.toList());
        log.info("推荐完成 - userId: {}, 食谱数量: {}, 食谱ID: {}", userId, recommendations.size(), recipeIds);

        return recommendations;
    }

    private List<RecipeScore> handleColdStart(HealthInfo healthInfo, List<RecipeScore> ruleScores, List<RecipeScore> contentScores) {
        List<RecipeScore> hotScores = getHotRecommendations();

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
        return scoreMap.entrySet().stream()
                .map(entry -> {
                    Recipe recipe = recipeMapper.selectById(entry.getKey());
                    return recipe != null ? new RecipeScore(recipe, entry.getValue()) : null;
                })
                .filter(Objects::nonNull)
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .limit(TOP_N)
                .collect(Collectors.toList());
    }

    private List<RecommendationVO> convertToVO(List<RecipeScore> scores, HealthInfo healthInfo) {
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
            vo.setReason(generateReason(healthInfo, bmi));
            result.add(vo);
        }

        return result;
    }

    private String generateReason(HealthInfo healthInfo, double bmi) {
        if (healthInfo != null && healthInfo.getGoal() != null) {
            String bmiStr = String.format("%.1f", bmi);
            return String.format("根据您的 BMI(%s) 和%s目标智能推荐", bmiStr, healthInfo.getGoal());
        }
        return "热门推荐";
    }
}
