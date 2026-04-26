package com.cupk.healthy_diet.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthInfoVO {
    private Integer id;
    private Integer userId;
    private Double height;
    private Double weight;
    private String gender;
    private String goal;
    private List<String> dietaryPreferences;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
