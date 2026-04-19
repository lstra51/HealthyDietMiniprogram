package com.cupk.healthy_diet.service;

import com.cupk.healthy_diet.vo.RecommendationVO;

import java.util.List;

public interface RecommendationService {
    List<RecommendationVO> getPersonalizedRecommendations(Integer userId);
}
