package com.cupk.healthy_diet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cupk.healthy_diet.entity.UserFavorite;
import com.cupk.healthy_diet.vo.RecipeVO;

import java.util.List;

public interface UserFavoriteService extends IService<UserFavorite> {

    boolean addFavorite(Integer userId, Integer recipeId);

    boolean removeFavorite(Integer userId, Integer recipeId);

    boolean isFavorited(Integer userId, Integer recipeId);

    List<RecipeVO> getUserFavorites(Integer userId);
}
