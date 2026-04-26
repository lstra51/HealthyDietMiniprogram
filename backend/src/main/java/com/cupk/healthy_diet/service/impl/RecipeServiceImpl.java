package com.cupk.healthy_diet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cupk.healthy_diet.dto.CreateRecipeDTO;
import com.cupk.healthy_diet.dto.RecipeImportDTO;
import com.cupk.healthy_diet.entity.*;
import com.cupk.healthy_diet.exception.BusinessException;
import com.cupk.healthy_diet.mapper.*;
import com.cupk.healthy_diet.service.RecipeService;
import com.cupk.healthy_diet.vo.RecipeDetailVO;
import com.cupk.healthy_diet.vo.RecipeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeServiceImpl extends ServiceImpl<RecipeMapper, Recipe> implements RecipeService {

    private final RecipeIngredientMapper recipeIngredientMapper;
    private final RecipeTagMapper recipeTagMapper;
    private final RecipeStepMapper recipeStepMapper;
    private final RecipeSuitableGoalMapper recipeSuitableGoalMapper;

    @Override
    public List<RecipeVO> getRecipeList(String category, String keyword, Integer minCalories, Integer maxCalories,
                                        Double minProtein, String tag, String goal) {
        LambdaQueryWrapper<Recipe> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Recipe::getStatus, "approved");
        
        if (category != null && !category.isEmpty() && !category.equals("全部")) {
            wrapper.eq(Recipe::getCategory, category);
        }
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Recipe::getName, keyword)
                    .or()
                    .like(Recipe::getDescription, keyword));
        }

        if (minCalories != null) {
            wrapper.ge(Recipe::getCalories, minCalories);
        }

        if (maxCalories != null) {
            wrapper.le(Recipe::getCalories, maxCalories);
        }

        if (minProtein != null) {
            wrapper.ge(Recipe::getProtein, minProtein);
        }

        Set<Integer> candidateIds = findRecipeIdsByTagAndGoal(tag, goal);
        if (candidateIds != null) {
            if (candidateIds.isEmpty()) {
                return List.of();
            }
            wrapper.in(Recipe::getId, candidateIds);
        }

        List<Recipe> recipes = this.list(wrapper);
        
        return recipes.stream().map(recipe -> {
            return convertToRecipeVO(recipe, getRecipeTags(recipe.getId()));
        }).collect(Collectors.toList());
    }

    @Override
    public RecipeDetailVO getRecipeDetail(Integer id) {
        Recipe recipe = this.getById(id);
        if (recipe == null) {
            throw new BusinessException("食谱不存在");
        }

        List<String> ingredients = getRecipeIngredients(id);
        List<String> tags = getRecipeTags(id);
        List<String> steps = getRecipeSteps(id);
        List<String> suitableGoals = getRecipeSuitableGoals(id);

        RecipeDetailVO vo = new RecipeDetailVO(
            recipe.getId(),
            recipe.getName(),
            recipe.getCategory(),
            recipe.getImage(),
            recipe.getDescription(),
            recipe.getCalories(),
            recipe.getProtein(),
            recipe.getCarbs(),
            recipe.getFat(),
            ingredients,
            tags,
            steps,
            suitableGoals,
            recipe.getStatus(),
            recipe.getRejectReason()
        );
        return vo;
    }

    @Override
    public List<String> getRecipeIngredients(Integer recipeId) {
        LambdaQueryWrapper<RecipeIngredient> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecipeIngredient::getRecipeId, recipeId);
        wrapper.orderByAsc(RecipeIngredient::getSortOrder);
        return recipeIngredientMapper.selectList(wrapper).stream()
                .map(RecipeIngredient::getIngredient)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getRecipeTags(Integer recipeId) {
        LambdaQueryWrapper<RecipeTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecipeTag::getRecipeId, recipeId);
        return recipeTagMapper.selectList(wrapper).stream()
                .map(RecipeTag::getTag)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getRecipeSteps(Integer recipeId) {
        LambdaQueryWrapper<RecipeStep> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecipeStep::getRecipeId, recipeId);
        wrapper.orderByAsc(RecipeStep::getStepNumber);
        return recipeStepMapper.selectList(wrapper).stream()
                .map(RecipeStep::getDescription)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getRecipeSuitableGoals(Integer recipeId) {
        LambdaQueryWrapper<RecipeSuitableGoal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecipeSuitableGoal::getRecipeId, recipeId);
        return recipeSuitableGoalMapper.selectList(wrapper).stream()
                .map(RecipeSuitableGoal::getGoal)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importRecipes(List<RecipeImportDTO> recipeDTOs) {
        for (RecipeImportDTO dto : recipeDTOs) {
            Recipe recipe = new Recipe();
            recipe.setName(dto.getName());
            recipe.setCategory(dto.getCategory());
            recipe.setImage(dto.getImage());
            recipe.setDescription(dto.getDescription());
            recipe.setCalories(dto.getCalories());
            recipe.setProtein(dto.getProtein());
            recipe.setCarbs(dto.getCarbs());
            recipe.setFat(dto.getFat());
            this.save(recipe);
            Integer recipeId = recipe.getId();

            if (dto.getIngredients() != null) {
                int sortOrder = 1;
                for (String ingredient : dto.getIngredients()) {
                    RecipeIngredient recipeIngredient = new RecipeIngredient();
                    recipeIngredient.setRecipeId(recipeId);
                    recipeIngredient.setIngredient(ingredient);
                    recipeIngredient.setSortOrder(sortOrder++);
                    recipeIngredientMapper.insert(recipeIngredient);
                }
            }

            if (dto.getTags() != null) {
                for (String tag : dto.getTags()) {
                    RecipeTag recipeTag = new RecipeTag();
                    recipeTag.setRecipeId(recipeId);
                    recipeTag.setTag(tag);
                    recipeTagMapper.insert(recipeTag);
                }
            }

            if (dto.getSuitableGoals() != null) {
                for (String goal : dto.getSuitableGoals()) {
                    RecipeSuitableGoal suitableGoal = new RecipeSuitableGoal();
                    suitableGoal.setRecipeId(recipeId);
                    suitableGoal.setGoal(goal);
                    recipeSuitableGoalMapper.insert(suitableGoal);
                }
            }

            if (dto.getSteps() != null) {
                int stepNumber = 1;
                for (String step : dto.getSteps()) {
                    RecipeStep recipeStep = new RecipeStep();
                    recipeStep.setRecipeId(recipeId);
                    recipeStep.setStepNumber(stepNumber++);
                    recipeStep.setDescription(step);
                    recipeStepMapper.insert(recipeStep);
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer createRecipe(CreateRecipeDTO dto, Integer userId) {
        Recipe recipe = new Recipe();
        recipe.setName(dto.getName());
        recipe.setCategory(dto.getCategory());
        recipe.setImage(dto.getImage());
        recipe.setDescription(dto.getDescription());
        recipe.setCalories(dto.getCalories());
        recipe.setProtein(dto.getProtein());
        recipe.setCarbs(dto.getCarbs());
        recipe.setFat(dto.getFat());
        recipe.setUserId(userId);
        recipe.setStatus("pending");
        this.save(recipe);
        Integer recipeId = recipe.getId();

        if (dto.getIngredients() != null) {
            int sortOrder = 1;
            for (String ingredient : dto.getIngredients()) {
                RecipeIngredient recipeIngredient = new RecipeIngredient();
                recipeIngredient.setRecipeId(recipeId);
                recipeIngredient.setIngredient(ingredient);
                recipeIngredient.setSortOrder(sortOrder++);
                recipeIngredientMapper.insert(recipeIngredient);
            }
        }

        if (dto.getTags() != null) {
            for (String tag : dto.getTags()) {
                RecipeTag recipeTag = new RecipeTag();
                recipeTag.setRecipeId(recipeId);
                recipeTag.setTag(tag);
                recipeTagMapper.insert(recipeTag);
            }
        }

        if (dto.getSuitableGoals() != null) {
            for (String goal : dto.getSuitableGoals()) {
                RecipeSuitableGoal suitableGoal = new RecipeSuitableGoal();
                suitableGoal.setRecipeId(recipeId);
                suitableGoal.setGoal(goal);
                recipeSuitableGoalMapper.insert(suitableGoal);
            }
        }

        if (dto.getSteps() != null) {
            int stepNumber = 1;
            for (String step : dto.getSteps()) {
                RecipeStep recipeStep = new RecipeStep();
                recipeStep.setRecipeId(recipeId);
                recipeStep.setStepNumber(stepNumber++);
                recipeStep.setDescription(step);
                recipeStepMapper.insert(recipeStep);
            }
        }

        return recipeId;
    }

    @Override
    public List<RecipeVO> getPendingRecipes() {
        LambdaQueryWrapper<Recipe> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Recipe::getStatus, "pending")
                .orderByDesc(Recipe::getCreatedAt);
        List<Recipe> recipes = this.list(wrapper);
        return recipes.stream().map(recipe -> {
            return convertToRecipeVO(recipe, getRecipeTags(recipe.getId()));
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveRecipe(Integer id) {
        Recipe recipe = this.getById(id);
        if (recipe == null) {
            throw new BusinessException("食谱不存在");
        }
        recipe.setStatus("approved");
        this.updateById(recipe);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectRecipe(Integer id, String reason) {
        Recipe recipe = this.getById(id);
        if (recipe == null) {
            throw new BusinessException("食谱不存在");
        }
        recipe.setStatus("rejected");
        recipe.setRejectReason(reason);
        this.updateById(recipe);
    }

    @Override
    public List<RecipeVO> getUserRecipes(Integer userId) {
        LambdaQueryWrapper<Recipe> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Recipe::getUserId, userId)
                .orderByDesc(Recipe::getCreatedAt);
        List<Recipe> recipes = this.list(wrapper);
        return recipes.stream().map(recipe -> {
            return convertToRecipeVO(recipe, getRecipeTags(recipe.getId()));
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRecipe(Integer id, CreateRecipeDTO dto, Integer userId) {
        Recipe recipe = this.getById(id);
        if (recipe == null) {
            throw new BusinessException("食谱不存在");
        }
        if (!recipe.getUserId().equals(userId)) {
            throw new BusinessException("无权修改此食谱");
        }

        recipe.setName(dto.getName());
        recipe.setCategory(dto.getCategory());
        recipe.setImage(dto.getImage());
        recipe.setDescription(dto.getDescription());
        recipe.setCalories(dto.getCalories());
        recipe.setProtein(dto.getProtein());
        recipe.setCarbs(dto.getCarbs());
        recipe.setFat(dto.getFat());
        recipe.setStatus("pending");
        recipe.setRejectReason(null);
        this.updateById(recipe);

        Integer recipeId = recipe.getId();

        recipeIngredientMapper.delete(new LambdaQueryWrapper<RecipeIngredient>().eq(RecipeIngredient::getRecipeId, recipeId));
        if (dto.getIngredients() != null) {
            int sortOrder = 1;
            for (String ingredient : dto.getIngredients()) {
                RecipeIngredient recipeIngredient = new RecipeIngredient();
                recipeIngredient.setRecipeId(recipeId);
                recipeIngredient.setIngredient(ingredient);
                recipeIngredient.setSortOrder(sortOrder++);
                recipeIngredientMapper.insert(recipeIngredient);
            }
        }

        recipeTagMapper.delete(new LambdaQueryWrapper<RecipeTag>().eq(RecipeTag::getRecipeId, recipeId));
        if (dto.getTags() != null) {
            for (String tag : dto.getTags()) {
                RecipeTag recipeTag = new RecipeTag();
                recipeTag.setRecipeId(recipeId);
                recipeTag.setTag(tag);
                recipeTagMapper.insert(recipeTag);
            }
        }

        recipeSuitableGoalMapper.delete(new LambdaQueryWrapper<RecipeSuitableGoal>().eq(RecipeSuitableGoal::getRecipeId, recipeId));
        if (dto.getSuitableGoals() != null) {
            for (String goal : dto.getSuitableGoals()) {
                RecipeSuitableGoal suitableGoal = new RecipeSuitableGoal();
                suitableGoal.setRecipeId(recipeId);
                suitableGoal.setGoal(goal);
                recipeSuitableGoalMapper.insert(suitableGoal);
            }
        }

        recipeStepMapper.delete(new LambdaQueryWrapper<RecipeStep>().eq(RecipeStep::getRecipeId, recipeId));
        if (dto.getSteps() != null) {
            int stepNumber = 1;
            for (String step : dto.getSteps()) {
                RecipeStep recipeStep = new RecipeStep();
                recipeStep.setRecipeId(recipeId);
                recipeStep.setStepNumber(stepNumber++);
                recipeStep.setDescription(step);
                recipeStepMapper.insert(recipeStep);
            }
        }
    }

    private Set<Integer> findRecipeIdsByTagAndGoal(String tag, String goal) {
        Set<Integer> ids = null;

        if (tag != null && !tag.isBlank()) {
            ids = recipeTagMapper.selectList(new LambdaQueryWrapper<RecipeTag>().eq(RecipeTag::getTag, tag.trim()))
                    .stream()
                    .map(RecipeTag::getRecipeId)
                    .collect(Collectors.toCollection(HashSet::new));
        }

        if (goal != null && !goal.isBlank()) {
            Set<Integer> goalIds = recipeSuitableGoalMapper.selectList(
                            new LambdaQueryWrapper<RecipeSuitableGoal>().eq(RecipeSuitableGoal::getGoal, goal.trim()))
                    .stream()
                    .map(RecipeSuitableGoal::getRecipeId)
                    .collect(Collectors.toCollection(HashSet::new));
            if (ids == null) {
                ids = goalIds;
            } else {
                ids.retainAll(goalIds);
            }
        }

        return ids;
    }

    private RecipeVO convertToRecipeVO(Recipe recipe, List<String> tags) {
        RecipeVO vo = new RecipeVO();
        vo.setId(recipe.getId());
        vo.setName(recipe.getName());
        vo.setCategory(recipe.getCategory());
        vo.setImage(recipe.getImage());
        vo.setCalories(recipe.getCalories());
        vo.setProtein(recipe.getProtein());
        vo.setCarbs(recipe.getCarbs());
        vo.setFat(recipe.getFat());
        vo.setTags(tags);
        vo.setStatus(recipe.getStatus());
        vo.setRejectReason(recipe.getRejectReason());
        return vo;
    }
}
