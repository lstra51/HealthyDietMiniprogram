package com.cupk.healthy_diet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cupk.healthy_diet.entity.Recipe;
import com.cupk.healthy_diet.entity.UserFavorite;
import com.cupk.healthy_diet.mapper.RecipeMapper;
import com.cupk.healthy_diet.mapper.UserFavoriteMapper;
import com.cupk.healthy_diet.service.UserFavoriteService;
import com.cupk.healthy_diet.vo.RecipeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserFavoriteServiceImpl extends ServiceImpl<UserFavoriteMapper, UserFavorite> implements UserFavoriteService {

    private final RecipeMapper recipeMapper;

    @Override
    public boolean addFavorite(Integer userId, Integer recipeId) {
        if (isFavorited(userId, recipeId)) {
            return true;
        }
        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(userId);
        favorite.setRecipeId(recipeId);
        return save(favorite);
    }

    @Override
    public boolean removeFavorite(Integer userId, Integer recipeId) {
        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getRecipeId, recipeId);
        return remove(wrapper);
    }

    @Override
    public boolean isFavorited(Integer userId, Integer recipeId) {
        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getRecipeId, recipeId);
        return count(wrapper) > 0;
    }

    @Override
    public List<RecipeVO> getUserFavorites(Integer userId) {
        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavorite::getUserId, userId)
                .orderByDesc(UserFavorite::getCreatedAt);
        List<UserFavorite> favorites = list(wrapper);

        if (favorites.isEmpty()) {
            return new ArrayList<>();
        }

        List<Integer> recipeIds = favorites.stream()
                .map(UserFavorite::getRecipeId)
                .collect(Collectors.toList());

        List<Recipe> recipes = recipeMapper.selectBatchIds(recipeIds);

        return recipes.stream().map(recipe -> {
            RecipeVO vo = new RecipeVO();
            vo.setId(recipe.getId());
            vo.setName(recipe.getName());
            vo.setCategory(recipe.getCategory());
            vo.setImage(recipe.getImage());
            vo.setCalories(recipe.getCalories());
            return vo;
        }).collect(Collectors.toList());
    }
}
