package com.cupk.healthy_diet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cupk.healthy_diet.entity.UserBehavior;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserBehaviorMapper extends BaseMapper<UserBehavior> {

    @Select("SELECT COUNT(*) FROM user_behavior WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Integer userId);

    @Select("SELECT recipe_id, COUNT(*) as cnt FROM user_behavior GROUP BY recipe_id ORDER BY cnt DESC LIMIT 20")
    List<Map<String, Object>> selectHotRecipes();
}
