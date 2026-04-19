package com.cupk.healthy_diet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cupk.healthy_diet.dto.DietRecordRequest;
import com.cupk.healthy_diet.entity.DietRecord;
import com.cupk.healthy_diet.vo.DailyNutritionVO;
import com.cupk.healthy_diet.vo.DietRecordVO;

import java.util.List;

public interface DietRecordService extends IService<DietRecord> {
    DietRecordVO addDietRecord(DietRecordRequest request);
    void deleteDietRecord(Integer id);
    List<DietRecordVO> getDietRecordsByUserIdAndDate(Integer userId, String date);
    List<DietRecordVO> getAllDietRecordsByUserId(Integer userId);
    DailyNutritionVO getDailyNutrition(Integer userId, String date);
}
