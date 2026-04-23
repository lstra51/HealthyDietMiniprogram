package com.cupk.healthy_diet.controller;

import com.cupk.healthy_diet.common.Result;
import com.cupk.healthy_diet.dto.UserBehaviorRequest;
import com.cupk.healthy_diet.exception.BusinessException;
import com.cupk.healthy_diet.security.AuthContext;
import com.cupk.healthy_diet.service.RecommendationService;
import com.cupk.healthy_diet.service.UserBehaviorService;
import com.cupk.healthy_diet.vo.RecommendationVO;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final UserBehaviorService userBehaviorService;

    @GetMapping("/user/{userId}")
    public Result<List<RecommendationVO>> getRecommendations(@PathVariable Integer userId,
                                                             @RequestAttribute(AuthContext.USER_ID) Integer currentUserId) {
        if (!userId.equals(currentUserId)) {
            throw new BusinessException(403, "无权访问其他用户的数据");
        }
        List<RecommendationVO> recommendations = recommendationService.getPersonalizedRecommendations(userId);
        return Result.success(recommendations);
    }

    @PostMapping("/behavior")
    public Result<Void> recordBehavior(@Valid @RequestBody UserBehaviorRequest request,
                                       @RequestAttribute(AuthContext.USER_ID) Integer userId) {
        userBehaviorService.recordBehavior(userId, request.getRecipeId(), request.getBehaviorType());
        return Result.success(null);
    }
}
