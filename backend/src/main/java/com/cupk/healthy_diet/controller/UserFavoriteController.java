package com.cupk.healthy_diet.controller;

import com.cupk.healthy_diet.common.Result;
import com.cupk.healthy_diet.dto.UserFavoriteRequest;
import com.cupk.healthy_diet.exception.BusinessException;
import com.cupk.healthy_diet.security.AuthContext;
import com.cupk.healthy_diet.service.UserFavoriteService;
import com.cupk.healthy_diet.vo.RecipeVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class UserFavoriteController {

    private final UserFavoriteService userFavoriteService;

    @PostMapping
    public Result<Void> addFavorite(@Valid @RequestBody UserFavoriteRequest request,
                                    @RequestAttribute(AuthContext.USER_ID) Integer userId) {
        boolean success = userFavoriteService.addFavorite(userId, request.getRecipeId());
        return success ? Result.success(null) : Result.error("收藏失败");
    }

    @DeleteMapping
    public Result<Void> removeFavorite(
            @RequestParam(required = false) Integer userId,
            @RequestParam Integer recipeId,
            @RequestAttribute(AuthContext.USER_ID) Integer currentUserId
    ) {
        boolean success = userFavoriteService.removeFavorite(currentUserId, recipeId);
        return success ? Result.success(null) : Result.error("取消收藏失败");
    }

    @GetMapping("/check")
    public Result<Boolean> isFavorited(
            @RequestParam(required = false) Integer userId,
            @RequestParam Integer recipeId,
            @RequestAttribute(AuthContext.USER_ID) Integer currentUserId
    ) {
        boolean favorited = userFavoriteService.isFavorited(currentUserId, recipeId);
        return Result.success(favorited);
    }

    @GetMapping("/user/{userId}")
    public Result<List<RecipeVO>> getUserFavorites(@PathVariable Integer userId,
                                                   @RequestAttribute(AuthContext.USER_ID) Integer currentUserId) {
        if (!userId.equals(currentUserId)) {
            throw new BusinessException(403, "无权访问其他用户的数据");
        }
        List<RecipeVO> favorites = userFavoriteService.getUserFavorites(userId);
        return Result.success(favorites);
    }
}
