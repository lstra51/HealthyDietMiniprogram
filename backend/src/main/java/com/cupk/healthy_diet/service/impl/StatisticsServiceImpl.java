package com.cupk.healthy_diet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cupk.healthy_diet.entity.DietRecord;
import com.cupk.healthy_diet.mapper.DietRecordMapper;
import com.cupk.healthy_diet.service.StatisticsService;
import com.cupk.healthy_diet.vo.NutritionPieVO;
import com.cupk.healthy_diet.vo.NutritionTrendVO;
import com.cupk.healthy_diet.vo.StatisticsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final DietRecordMapper dietRecordMapper;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public StatisticsVO getWeeklyStatistics(Integer userId) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);
        return getStatistics(userId, startDate, endDate);
    }

    @Override
    public StatisticsVO getMonthlyStatistics(Integer userId) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(29);
        return getStatistics(userId, startDate, endDate);
    }

    private StatisticsVO getStatistics(Integer userId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<DietRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DietRecord::getUserId, userId)
                .between(DietRecord::getRecordDate, startDate, endDate)
                .orderByAsc(DietRecord::getRecordDate);

        List<DietRecord> records = dietRecordMapper.selectList(wrapper);

        Map<LocalDate, DailySummary> dailySummaries = new HashMap<>();
        LocalDate date = startDate;
        while (!date.isAfter(endDate)) {
            dailySummaries.put(date, new DailySummary());
            date = date.plusDays(1);
        }

        for (DietRecord record : records) {
            DailySummary summary = dailySummaries.get(record.getRecordDate());
            if (summary != null) {
                summary.addRecord(record);
            }
        }

        List<String> dates = new ArrayList<>();
        List<Integer> calories = new ArrayList<>();
        List<Double> protein = new ArrayList<>();
        List<Double> carbs = new ArrayList<>();
        List<Double> fat = new ArrayList<>();

        double totalCalories = 0;
        double totalProtein = 0;
        double totalCarbs = 0;
        double totalFat = 0;
        int daysWithData = 0;

        date = startDate;
        while (!date.isAfter(endDate)) {
            DailySummary summary = dailySummaries.get(date);
            dates.add(date.format(DATE_FORMATTER));
            calories.add(summary.calories);
            protein.add(summary.protein);
            carbs.add(summary.carbs);
            fat.add(summary.fat);

            if (summary.calories > 0) {
                totalCalories += summary.calories;
                totalProtein += summary.protein;
                totalCarbs += summary.carbs;
                totalFat += summary.fat;
                daysWithData++;
            }

            date = date.plusDays(1);
        }

        double avgCalories = daysWithData > 0 ? totalCalories / daysWithData : 0;
        double avgProtein = daysWithData > 0 ? totalProtein / daysWithData : 0;
        double avgCarbs = daysWithData > 0 ? totalCarbs / daysWithData : 0;
        double avgFat = daysWithData > 0 ? totalFat / daysWithData : 0;

        List<NutritionPieVO> pieData = new ArrayList<>();
        double totalNutrition = avgProtein * 4 + avgCarbs * 4 + avgFat * 9;
        if (totalNutrition > 0) {
            pieData.add(NutritionPieVO.builder()
                    .name("蛋白质")
                    .value(avgProtein * 4)
                    .percentage((avgProtein * 4 / totalNutrition) * 100)
                    .build());
            pieData.add(NutritionPieVO.builder()
                    .name("碳水化合物")
                    .value(avgCarbs * 4)
                    .percentage((avgCarbs * 4 / totalNutrition) * 100)
                    .build());
            pieData.add(NutritionPieVO.builder()
                    .name("脂肪")
                    .value(avgFat * 9)
                    .percentage((avgFat * 9 / totalNutrition) * 100)
                    .build());
        }

        NutritionTrendVO trend = NutritionTrendVO.builder()
                .dates(dates)
                .calories(calories)
                .protein(protein)
                .carbs(carbs)
                .fat(fat)
                .build();

        return StatisticsVO.builder()
                .trend(trend)
                .pieData(pieData)
                .avgCalories(avgCalories)
                .avgProtein(avgProtein)
                .avgCarbs(avgCarbs)
                .avgFat(avgFat)
                .build();
    }

    private static class DailySummary {
        int calories = 0;
        double protein = 0;
        double carbs = 0;
        double fat = 0;

        void addRecord(DietRecord record) {
            double portion = record.getPortion() != null ? record.getPortion() : 1.0;
            calories += (int) (record.getCalories() * portion);
            protein += record.getProtein() * portion;
            carbs += record.getCarbs() * portion;
            fat += record.getFat() * portion;
        }
    }
}
