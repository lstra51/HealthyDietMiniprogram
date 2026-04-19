package com.cupk.healthy_diet.controller;

import com.cupk.healthy_diet.common.Result;
import com.cupk.healthy_diet.service.RecipeService;
import com.cupk.healthy_diet.vo.RecipeDetailVO;
import com.cupk.healthy_diet.vo.RecipeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;

    @GetMapping
    public Result<List<RecipeVO>> getRecipeList(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        List<RecipeVO> recipes = recipeService.getRecipeList(category, keyword);
        return Result.success(recipes);
    }

    @GetMapping("/{id}")
    public Result<RecipeDetailVO> getRecipeDetail(@PathVariable Integer id) {
        RecipeDetailVO recipe = recipeService.getRecipeDetail(id);
        return Result.success(recipe);
    }
}
