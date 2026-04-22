package com.cupk.healthy_diet.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeVO {
    private Integer id;
    private String name;
    private String category;
    private String image;
    private Integer calories;
    private List<String> tags;
    private String status;
    private String rejectReason;
}
