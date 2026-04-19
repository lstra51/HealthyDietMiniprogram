package com.cupk.healthy_diet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cupk.healthy_diet.entity.UserBehavior;

import java.util.List;
import java.util.Map;

public interface UserBehaviorService extends IService<UserBehavior> {

    void recordBehavior(Integer userId, Integer recipeId, String behaviorType);

    boolean isNewUser(Integer userId);

    List<Map<String, Object>> getHotRecipes();
}
