package com.cupk.healthy_diet.controller;

import com.cupk.healthy_diet.common.Result;
import com.cupk.healthy_diet.service.StatisticsService;
import com.cupk.healthy_diet.vo.StatisticsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/weekly/{userId}")
    public Result<StatisticsVO> getWeeklyStatistics(@PathVariable Integer userId) {
        StatisticsVO statistics = statisticsService.getWeeklyStatistics(userId);
        return Result.success(statistics);
    }

    @GetMapping("/monthly/{userId}")
    public Result<StatisticsVO> getMonthlyStatistics(@PathVariable Integer userId) {
        StatisticsVO statistics = statisticsService.getMonthlyStatistics(userId);
        return Result.success(statistics);
    }
}
