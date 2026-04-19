package com.cupk.healthy_diet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cupk.healthy_diet.entity.Recipe;
import com.cupk.healthy_diet.entity.RecipeScore;
import com.cupk.healthy_diet.entity.UserBehavior;
import com.cupk.healthy_diet.mapper.RecipeMapper;
import com.cupk.healthy_diet.mapper.UserBehaviorMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CFRecommendService {

    private final UserBehaviorMapper userBehaviorMapper;
    private final RecipeMapper recipeMapper;

    public List<RecipeScore> recommend(Integer userId) {
        log.info("协同过滤: 开始为用户 {} 计算推荐", userId);
        
        Map<Long, Map<Long, Integer>> userVectors = buildUserVectors();
        log.info("协同过滤: 系统共有 {} 个用户有行为数据", userVectors.size());
        
        if (!userVectors.containsKey(userId.longValue())) {
            log.info("协同过滤: 用户 {} 不在行为向量中，返回空结果", userId);
            return new ArrayList<>();
        }

        Map<Long, Integer> targetVector = userVectors.get(userId.longValue());
        log.info("协同过滤: 目标用户 {} 的行为向量包含 {} 个食谱", userId, targetVector.size());

        Map<Long, Double> similarityMap = new HashMap<>();

        for (Map.Entry<Long, Map<Long, Integer>> entry : userVectors.entrySet()) {
            Long otherId = entry.getKey();
            if (otherId.equals(userId.longValue())) continue;
            double sim = cosineSimilarity(targetVector, entry.getValue());
            similarityMap.put(otherId, sim);
        }
        
        log.info("协同过滤: 计算了 {} 个其他用户的相似度", similarityMap.size());

        List<Long> similarUsers = topK(similarityMap, 10);
        log.info("协同过滤: Top 5 相似用户: {}", similarUsers.stream().limit(5).collect(Collectors.toList()));

        Map<Long, Double> recipeScoreMap = new HashMap<>();
// 计算其他用户的推荐，食谱得分 = Σ (相似用户相似度 × 该用户对食谱的行为权重)
        for (Long simUser : similarUsers) {
            Double similarity = similarityMap.get(simUser);
            Map<Long, Integer> simUserVector = userVectors.get(simUser);
            log.info("协同过滤: 相似用户 {}: 相似度={}, 行为数={}", 
                    simUser, String.format("%.4f", similarity), simUserVector.size());
            
            for (Map.Entry<Long, Integer> entry : simUserVector.entrySet()) {
                Long recipeId = entry.getKey();
                if (!targetVector.containsKey(recipeId)) {
                    double score = similarity * entry.getValue();
                    recipeScoreMap.merge(recipeId, score, Double::sum);
                }
            }
        }
        
        log.info("协同过滤: 共推荐 {} 个新食谱", recipeScoreMap.size());

        List<RecipeScore> result = new ArrayList<>();
        for (Map.Entry<Long, Double> entry : recipeScoreMap.entrySet()) {
            Recipe recipe = recipeMapper.selectById(entry.getKey().intValue());
            if (recipe != null) {
                result.add(new RecipeScore(recipe, entry.getValue()));
            }
        }

        List<RecipeScore> sortedResult = result.stream()
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .collect(Collectors.toList());
        
        log.info("协同过滤: 最终推荐 {} 个食谱", sortedResult.size());
        return sortedResult;
    }

    private Map<Long, Map<Long, Integer>> buildUserVectors() {
        Map<Long, Map<Long, Integer>> userVectors = new HashMap<>();

        List<UserBehavior> behaviors = userBehaviorMapper.selectList(null);
        log.info("协同过滤: 数据库中共有 {} 条行为记录", behaviors.size());

        for (UserBehavior behavior : behaviors) {
            Long userId = behavior.getUserId().longValue();
            Long recipeId = behavior.getRecipeId().longValue();
            Integer weight = behavior.getWeight();

            userVectors.computeIfAbsent(userId, k -> new HashMap<>());
            Map<Long, Integer> userVector = userVectors.get(userId);
            userVector.merge(recipeId, weight, Integer::sum);
        }

        return userVectors;
    }// 构建用户行为向量

    private double cosineSimilarity(Map<Long, Integer> u1, Map<Long, Integer> u2) {
        Set<Long> common = new HashSet<>(u1.keySet());
        common.retainAll(u2.keySet());

        double dot = 0;
        for (Long key : common) {
            dot += u1.get(key) * u2.get(key);
        }

        double norm1 = Math.sqrt(u1.values().stream().mapToDouble(v -> v * v).sum());
        double norm2 = Math.sqrt(u2.values().stream().mapToDouble(v -> v * v).sum());

        return dot / (norm1 * norm2 + 1e-9);
    }// 计算两个向量的余弦相似度

    private List<Long> topK(Map<Long, Double> similarityMap, int k) {
        return similarityMap.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(k)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
