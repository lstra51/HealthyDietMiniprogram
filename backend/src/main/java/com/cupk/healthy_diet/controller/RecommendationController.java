package com.cupk.healthy_diet.controller;

import com.cupk.healthy_diet.common.Result;
import com.cupk.healthy_diet.dto.UserBehaviorRequest;
import com.cupk.healthy_diet.service.RecommendationService;
import com.cupk.healthy_diet.service.UserBehaviorService;
import com.cupk.healthy_diet.vo.RecommendationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final UserBehaviorService userBehaviorService;

    @GetMapping("/user/{userId}")
    public Result<List<RecommendationVO>> getRecommendations(@PathVariable Integer userId) {
        List<RecommendationVO> recommendations = recommendationService.getPersonalizedRecommendations(userId);
        return Result.success(recommendations);
    }

    @PostMapping("/behavior")
    public Result<Void> recordBehavior(@RequestBody UserBehaviorRequest request) {
        userBehaviorService.recordBehavior(request.getUserId(), request.getRecipeId(), request.getBehaviorType());
        return Result.success(null);
    }
}
