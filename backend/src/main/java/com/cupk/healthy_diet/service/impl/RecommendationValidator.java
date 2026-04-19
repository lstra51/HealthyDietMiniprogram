package com.cupk.healthy_diet.service.impl;

import com.cupk.healthy_diet.entity.HealthInfo;
import com.cupk.healthy_diet.entity.RecipeScore;
import com.cupk.healthy_diet.entity.UserBehavior;
import com.cupk.healthy_diet.mapper.UserBehaviorMapper;
import com.cupk.healthy_diet.service.HealthInfoService;
import com.cupk.healthy_diet.service.UserBehaviorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationValidator {

    private final RuleRecommendService ruleRecommendService;
    private final ContentRecommendService contentRecommendService;
    private final CFRecommendService cfRecommendService;
    private final HealthInfoService healthInfoService;
    private final UserBehaviorService userBehaviorService;
    private final UserBehaviorMapper userBehaviorMapper;

    private static final double RULE_WEIGHT = 0.4;
    private static final double CONTENT_WEIGHT = 0.3;
    private static final double CF_WEIGHT = 0.3;

    public void validateAndLog(Integer userId, List<RecipeScore> ruleScores, List<RecipeScore> contentScores, List<RecipeScore> cfScores) {
        log.info("混合推荐: 开始验证 - 用户ID: {}", userId);

        HealthInfo healthInfo = healthInfoService.getHealthInfoByUserId(userId);
        boolean isNewUser = userBehaviorService.isNewUser(userId);

        logUserBasicInfo(userId, healthInfo, isNewUser);
        logUserBehaviors(userId);

        logRecommendationResults("规则推荐 (权重: " + RULE_WEIGHT + ")", ruleScores, RULE_WEIGHT);
        logRecommendationResults("内容推荐 (权重: " + CONTENT_WEIGHT + ")", contentScores, CONTENT_WEIGHT);
        
        if (!isNewUser) {
            logRecommendationResults("协同过滤 (权重: " + CF_WEIGHT + ")", cfScores, CF_WEIGHT);
        } else {
            log.info("协同过滤: 新用户，无数据");
        }

        logFinalMergedResults(ruleScores, contentScores, cfScores, isNewUser);

        log.info("混合推荐: 验证完成");
    }

    private void logUserBasicInfo(Integer userId, HealthInfo healthInfo, boolean isNewUser) {
        log.info("用户信息: ID={}, 新用户={}", userId, isNewUser ? "是 (冷启动)" : "否 (混合推荐)");
        
        if (healthInfo != null) {
            log.info("健康信息: 目标={}", healthInfo.getGoal());
            if (healthInfo.getHeight() != null && healthInfo.getWeight() != null) {
                double bmi = healthInfo.getWeight() / ((healthInfo.getHeight() / 100.0) * (healthInfo.getHeight() / 100.0));
                log.info("BMI: {}", String.format("%.1f", bmi));
            }
        } else {
            log.info("健康信息: 未设置");
        }
    }

    private void logUserBehaviors(Integer userId) {
        List<UserBehavior> behaviors = userBehaviorMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserBehavior>()
                        .eq(UserBehavior::getUserId, userId)
                        .orderByDesc(UserBehavior::getCreateTime)
        );

        if (behaviors.isEmpty()) {
            log.info("用户行为: 无记录");
        } else {
            Map<String, Long> behaviorCount = behaviors.stream()
                    .collect(Collectors.groupingBy(UserBehavior::getBehaviorType, Collectors.counting()));
            
            log.info("用户行为: 统计 view={}, click={}, like={}, cook={}",
                    behaviorCount.getOrDefault("view", 0L),
                    behaviorCount.getOrDefault("click", 0L),
                    behaviorCount.getOrDefault("like", 0L),
                    behaviorCount.getOrDefault("cook", 0L));
            
            log.info("用户行为: 最近5条");
            behaviors.stream().limit(5).forEach(b -> {
                log.info("  - 食谱ID: {}, 类型: {}, 权重: {}, 时间: {}",
                        b.getRecipeId(), b.getBehaviorType(), b.getWeight(), b.getCreateTime());
            });
        }
    }

    private void logRecommendationResults(String title, List<RecipeScore> scores, double weight) {
        log.info("{}", title);
        
        if (scores.isEmpty()) {
            log.info("  无推荐结果");
        } else {
            log.info("  Top 5 推荐:");
            scores.stream().limit(5).forEach(rs -> {
                String rawScoreStr = String.format("%.4f", rs.getScore());
                String weightedScoreStr = String.format("%.4f", rs.getScore() * weight);
                log.info("    - 食谱ID: {}, 名称: {}, 原始分: {}, 加权分: {}",
                        rs.getRecipe().getId(),
                        rs.getRecipe().getName(),
                        rawScoreStr,
                        weightedScoreStr);
            });
        }
    }

    private void logFinalMergedResults(List<RecipeScore> ruleScores, 
                                        List<RecipeScore> contentScores, 
                                        List<RecipeScore> cfScores,
                                        boolean isNewUser) {
        log.info("最终融合结果");
        
        Map<Integer, Double> scoreMap = new java.util.HashMap<>();
        
        mergeScores(scoreMap, ruleScores, RULE_WEIGHT);
        mergeScores(scoreMap, contentScores, CONTENT_WEIGHT);
        
        if (!isNewUser) {
            mergeScores(scoreMap, cfScores, CF_WEIGHT);
        }

        List<Map.Entry<Integer, Double>> sortedScores = scoreMap.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(10)
                .collect(Collectors.toList());

        if (sortedScores.isEmpty()) {
            log.info("  无融合结果");
        } else {
            log.info("  Top 10 融合推荐:");
            int rank = 1;
            for (Map.Entry<Integer, Double> entry : sortedScores) {
                String scoreStr = String.format("%.4f", entry.getValue());
                log.info("    {}. 食谱ID: {}, 最终得分: {}",
                        rank++, entry.getKey(), scoreStr);
            }
        }
    }

    private void mergeScores(Map<Integer, Double> scoreMap, List<RecipeScore> scores, double weight) {
        for (RecipeScore rs : scores) {
            Integer recipeId = rs.getRecipe().getId();
            double currentScore = scoreMap.getOrDefault(recipeId, 0.0);
            scoreMap.put(recipeId, currentScore + rs.getScore() * weight);
        }
    }
}
