package com.cupk.healthy_diet.controller;

import com.cupk.healthy_diet.common.Result;
import com.cupk.healthy_diet.dto.UserFavoriteRequest;
import com.cupk.healthy_diet.service.UserFavoriteService;
import com.cupk.healthy_diet.vo.RecipeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class UserFavoriteController {

    private final UserFavoriteService userFavoriteService;

    @PostMapping
    public Result<Void> addFavorite(@RequestBody UserFavoriteRequest request) {
        boolean success = userFavoriteService.addFavorite(request.getUserId(), request.getRecipeId());
        return success ? Result.success(null) : Result.error("收藏失败");
    }

    @DeleteMapping
    public Result<Void> removeFavorite(
            @RequestParam Integer userId,
            @RequestParam Integer recipeId
    ) {
        boolean success = userFavoriteService.removeFavorite(userId, recipeId);
        return success ? Result.success(null) : Result.error("取消收藏失败");
    }

    @GetMapping("/check")
    public Result<Boolean> isFavorited(
            @RequestParam Integer userId,
            @RequestParam Integer recipeId
    ) {
        boolean favorited = userFavoriteService.isFavorited(userId, recipeId);
        return Result.success(favorited);
    }

    @GetMapping("/user/{userId}")
    public Result<List<RecipeVO>> getUserFavorites(@PathVariable Integer userId) {
        List<RecipeVO> favorites = userFavoriteService.getUserFavorites(userId);
        return Result.success(favorites);
    }
}
