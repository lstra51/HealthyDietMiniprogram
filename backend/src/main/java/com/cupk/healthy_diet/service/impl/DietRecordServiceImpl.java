package com.cupk.healthy_diet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cupk.healthy_diet.dto.DietRecordRequest;
import com.cupk.healthy_diet.entity.DietRecord;
import com.cupk.healthy_diet.exception.BusinessException;
import com.cupk.healthy_diet.mapper.DietRecordMapper;
import com.cupk.healthy_diet.service.DietRecordService;
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

        return new DailyNutritionVO(totalCalories, totalProtein, totalCarbs, totalFat);
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
}
