package com.cupk.healthy_diet.controller;

import com.cupk.healthy_diet.common.Result;
import com.cupk.healthy_diet.dto.DietRecordRequest;
import com.cupk.healthy_diet.service.DietRecordService;
import com.cupk.healthy_diet.vo.DailyNutritionVO;
import com.cupk.healthy_diet.vo.DietRecordVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class DietRecordController {

    private final DietRecordService dietRecordService;

    @PostMapping
    public Result<DietRecordVO> addDietRecord(@Valid @RequestBody DietRecordRequest request) {
        DietRecordVO record = dietRecordService.addDietRecord(request);
        return Result.success("记录成功", record);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteDietRecord(@PathVariable Integer id) {
        dietRecordService.deleteDietRecord(id);
        return Result.success("删除成功", null);
    }

    @GetMapping("/user/{userId}/date/{date}")
    public Result<List<DietRecordVO>> getDietRecordsByDate(
            @PathVariable Integer userId,
            @PathVariable String date) {
        List<DietRecordVO> records = dietRecordService.getDietRecordsByUserIdAndDate(userId, date);
        return Result.success(records);
    }

    @GetMapping("/user/{userId}")
    public Result<List<DietRecordVO>> getAllDietRecords(@PathVariable Integer userId) {
        List<DietRecordVO> records = dietRecordService.getAllDietRecordsByUserId(userId);
        return Result.success(records);
    }

    @GetMapping("/user/{userId}/nutrition/{date}")
    public Result<DailyNutritionVO> getDailyNutrition(
            @PathVariable Integer userId,
            @PathVariable String date) {
        DailyNutritionVO nutrition = dietRecordService.getDailyNutrition(userId, date);
        return Result.success(nutrition);
    }
}
