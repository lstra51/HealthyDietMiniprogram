package com.cupk.healthy_diet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cupk.healthy_diet.entity.Recipe;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RecipeMapper extends BaseMapper<Recipe> {
}
