package com.cupk.healthy_diet.service;

import com.cupk.healthy_diet.vo.StatisticsVO;

public interface StatisticsService {
    StatisticsVO getWeeklyStatistics(Integer userId);
    StatisticsVO getMonthlyStatistics(Integer userId);
}
