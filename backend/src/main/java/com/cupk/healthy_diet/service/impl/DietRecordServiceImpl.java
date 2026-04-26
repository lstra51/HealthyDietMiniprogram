package com.cupk.healthy_diet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cupk.healthy_diet.dto.DietRecordRequest;
import com.cupk.healthy_diet.entity.DietRecord;
import com.cupk.healthy_diet.entity.HealthInfo;
import com.cupk.healthy_diet.exception.BusinessException;
import com.cupk.healthy_diet.mapper.DietRecordMapper;
import com.cupk.healthy_diet.service.DietRecordService;
import com.cupk.healthy_diet.service.HealthInfoService;
import com.cupk.healthy_diet.vo.DailyNutritionVO;
import com.cupk.healthy_diet.vo.DietRecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DietRecordServiceImpl extends ServiceImpl<DietRecordMapper, DietRecord> implements DietRecordService {

    private final HealthInfoService healthInfoService;

    @Override
    public DietRecordVO addDietRecord(DietRecordRequest request) {
        DietRecord record = new DietRecord();
        record.setUserId(request.getUserId());
        record.setRecipeId(request.getRecipeId());
        record.setRecipeName(request.getRecipeName());
        record.setMealType(request.getMealType());
        record.setPortion(request.getPortion());
        record.setCalories(request.getCalories());
        record.setProtein(request.getProtein());
        record.setCarbs(request.getCarbs());
        record.setFat(request.getFat());
        record.setRecordDate(LocalDate.parse(request.getRecordDate()));

        this.save(record);

        return convertToVO(record);
    }

    @Override
    public void deleteDietRecord(Integer id) {
        DietRecord record = this.getById(id);
        if (record == null) {
            throw new BusinessException("记录不存在");
        }
        this.removeById(id);
    }

    @Override
    public List<DietRecordVO> getDietRecordsByUserIdAndDate(Integer userId, String date) {
        LambdaQueryWrapper<DietRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DietRecord::getUserId, userId);
        wrapper.eq(DietRecord::getRecordDate, LocalDate.parse(date));
        wrapper.orderByDesc(DietRecord::getCreatedAt);

        List<DietRecord> records = this.list(wrapper);
        if (records == null) {
            return new ArrayList<>();
        }
        
        return records.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<DietRecordVO> getAllDietRecordsByUserId(Integer userId) {
        LambdaQueryWrapper<DietRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DietRecord::getUserId, userId);
        wrapper.orderByDesc(DietRecord::getRecordDate);
        wrapper.orderByDesc(DietRecord::getCreatedAt);

        List<DietRecord> records = this.list(wrapper);
        if (records == null) {
            return new ArrayList<>();
        }
        
        return records.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public DailyNutritionVO getDailyNutrition(Integer userId, String date) {
        List<DietRecordVO> records = getDietRecordsByUserIdAndDate(userId, date);

        int totalCalories = records.stream()
                .mapToInt(record -> (int) Math.round(record.getCalories() * getPortion(record)))
                .sum();
        double totalProtein = records.stream().mapToDouble(record -> record.getProtein() * getPortion(record)).sum();
        double totalCarbs = records.stream().mapToDouble(record -> record.getCarbs() * getPortion(record)).sum();
        double totalFat = records.stream().mapToDouble(record -> record.getFat() * getPortion(record)).sum();

        NutritionTarget target = calculateTarget(healthInfoService.getHealthInfoByUserId(userId));

        return new DailyNutritionVO(
                totalCalories,
                round(totalProtein),
                round(totalCarbs),
                round(totalFat),
                target.calories,
                target.protein,
                target.carbs,
                target.fat,
                progress(totalCalories, target.calories),
                progress(totalProtein, target.protein),
                progress(totalCarbs, target.carbs),
                progress(totalFat, target.fat)
        );
    }

    private double getPortion(DietRecordVO record) {
        return record.getPortion() != null ? record.getPortion() : 1.0;
    }

    private DietRecordVO convertToVO(DietRecord record) {
        return new DietRecordVO(
                record.getId(),
                record.getRecipeId(),
                record.getRecipeName(),
                record.getMealType(),
                record.getPortion(),
                record.getCalories(),
                record.getProtein(),
                record.getCarbs(),
                record.getFat(),
                record.getRecordDate()
        );
    }

    private NutritionTarget calculateTarget(HealthInfo healthInfo) {
        if (healthInfo == null || healthInfo.getHeight() == null || healthInfo.getWeight() == null) {
            return new NutritionTarget(2000, 100.0, 250.0, 55.0);
        }

        double height = healthInfo.getHeight();
        double weight = healthInfo.getWeight();
        int age = 30;
        boolean female = "女".equals(healthInfo.getGender());
        double bmr = female
                ? 10 * weight + 6.25 * height - 5 * age - 161
                : 10 * weight + 6.25 * height - 5 * age + 5;
        double calories = bmr * 1.35;

        String goal = healthInfo.getGoal();
        if ("减脂".equals(goal)) {
            calories -= 300;
        } else if ("增肌".equals(goal)) {
            calories += 250;
        }
        calories = Math.max(1200, calories);

        double proteinPerKg = "增肌".equals(goal) ? 2.0 : ("减脂".equals(goal) ? 1.8 : 1.4);
        double protein = weight * proteinPerKg;
        double fat = calories * 0.25 / 9;
        double carbs = Math.max(0, (calories - protein * 4 - fat * 9) / 4);

        return new NutritionTarget(
                (int) Math.round(calories),
                round(protein),
                round(carbs),
                round(fat)
        );
    }

    private double progress(double value, double target) {
        if (target <= 0) {
            return 0.0;
        }
        return round(Math.min(value / target * 100, 999));
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private record NutritionTarget(Integer calories, Double protein, Double carbs, Double fat) {
    }
}
