package com.cupk.healthy_diet.service;

import com.cupk.healthy_diet.vo.DishRecognitionVO;
import org.springframework.web.multipart.MultipartFile;

public interface DishRecognitionService {
    DishRecognitionVO recognize(MultipartFile file);
}
