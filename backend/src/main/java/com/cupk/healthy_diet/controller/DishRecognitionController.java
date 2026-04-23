package com.cupk.healthy_diet.controller;

import com.cupk.healthy_diet.common.Result;
import com.cupk.healthy_diet.service.DishRecognitionService;
import com.cupk.healthy_diet.vo.DishRecognitionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/nutrition")
@RequiredArgsConstructor
public class DishRecognitionController {

    private final DishRecognitionService dishRecognitionService;

    @PostMapping("/recognize")
    public Result<DishRecognitionVO> recognizeDish(@RequestParam("file") MultipartFile file) {
        return Result.success(dishRecognitionService.recognize(file));
    }
}
