package com.cupk.healthy_diet.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cupk.healthy_diet.entity.UserBehavior;
import com.cupk.healthy_diet.mapper.UserBehaviorMapper;
import com.cupk.healthy_diet.service.UserBehaviorService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserBehaviorServiceImpl extends ServiceImpl<UserBehaviorMapper, UserBehavior> implements UserBehaviorService {

    private static final Map<String, Integer> BEHAVIOR_WEIGHTS = new HashMap<>();

    static {
        BEHAVIOR_WEIGHTS.put("view", 1);
        BEHAVIOR_WEIGHTS.put("click", 2);
        BEHAVIOR_WEIGHTS.put("like", 3);
        BEHAVIOR_WEIGHTS.put("cook", 5);
    }

    @Override
    public void recordBehavior(Integer userId, Integer recipeId, String behaviorType) {
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setRecipeId(recipeId);
        behavior.setBehaviorType(behaviorType);
        behavior.setWeight(BEHAVIOR_WEIGHTS.getOrDefault(behaviorType, 1));
        save(behavior);
    }

    @Override
    public boolean isNewUser(Integer userId) {
        return baseMapper.countByUserId(userId) < 2;
    }

    @Override
    public List<Map<String, Object>> getHotRecipes() {
        return baseMapper.selectHotRecipes();
    }
}
