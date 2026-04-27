package com.cupk.healthy_diet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cupk.healthy_diet.dto.CreateRecipeDTO;
import com.cupk.healthy_diet.dto.RecipeImportDTO;
import com.cupk.healthy_diet.entity.Recipe;
import com.cupk.healthy_diet.entity.RecipeIngredient;
import com.cupk.healthy_diet.entity.RecipeStep;
import com.cupk.healthy_diet.entity.RecipeSuitableGoal;
import com.cupk.healthy_diet.entity.RecipeTag;
import com.cupk.healthy_diet.exception.BusinessException;
import com.cupk.healthy_diet.mapper.RecipeIngredientMapper;
import com.cupk.healthy_diet.mapper.RecipeMapper;
import com.cupk.healthy_diet.mapper.RecipeStepMapper;
import com.cupk.healthy_diet.mapper.RecipeSuitableGoalMapper;
import com.cupk.healthy_diet.mapper.RecipeTagMapper;
import com.cupk.healthy_diet.service.RecipeService;
import com.cupk.healthy_diet.vo.RecipeDetailVO;
import com.cupk.healthy_diet.vo.RecipeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
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

        return this.list(wrapper).stream()
                .map(recipe -> convertToRecipeVO(recipe, getRecipeTags(recipe.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public RecipeDetailVO getRecipeDetail(Integer id) {
        Recipe recipe = requireRecipe(id);
        return new RecipeDetailVO(
                recipe.getId(),
                recipe.getName(),
                recipe.getCategory(),
                recipe.getImage(),
                recipe.getDescription(),
                recipe.getCalories(),
                recipe.getProtein(),
                recipe.getCarbs(),
                recipe.getFat(),
                getRecipeIngredients(id),
                getRecipeTags(id),
                getRecipeSteps(id),
                getRecipeSuitableGoals(id),
                recipe.getStatus(),
                recipe.getRejectReason()
        );
    }

    @Override
    public List<String> getRecipeIngredients(Integer recipeId) {
        return recipeIngredientMapper.selectList(new LambdaQueryWrapper<RecipeIngredient>()
                        .eq(RecipeIngredient::getRecipeId, recipeId)
                        .orderByAsc(RecipeIngredient::getSortOrder))
                .stream()
                .map(RecipeIngredient::getIngredient)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getRecipeTags(Integer recipeId) {
        return recipeTagMapper.selectList(new LambdaQueryWrapper<RecipeTag>()
                        .eq(RecipeTag::getRecipeId, recipeId))
                .stream()
                .map(RecipeTag::getTag)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getRecipeSteps(Integer recipeId) {
        return recipeStepMapper.selectList(new LambdaQueryWrapper<RecipeStep>()
                        .eq(RecipeStep::getRecipeId, recipeId)
                        .orderByAsc(RecipeStep::getStepNumber))
                .stream()
                .map(RecipeStep::getDescription)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getRecipeSuitableGoals(Integer recipeId) {
        return recipeSuitableGoalMapper.selectList(new LambdaQueryWrapper<RecipeSuitableGoal>()
                        .eq(RecipeSuitableGoal::getRecipeId, recipeId))
                .stream()
                .map(RecipeSuitableGoal::getGoal)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getAvailableTags() {
        List<Integer> approvedRecipeIds = this.list(new LambdaQueryWrapper<Recipe>()
                        .select(Recipe::getId)
                        .eq(Recipe::getStatus, "approved"))
                .stream()
                .map(Recipe::getId)
                .collect(Collectors.toList());
        if (approvedRecipeIds.isEmpty()) {
            return List.of();
        }

        Map<String, Long> tagCountMap = recipeTagMapper.selectList(new LambdaQueryWrapper<RecipeTag>()
                        .in(RecipeTag::getRecipeId, approvedRecipeIds))
                .stream()
                .map(RecipeTag::getTag)
                .filter(tag -> tag != null && !tag.isBlank())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return tagCountMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .map(Map.Entry::getKey)
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
            replaceRecipeRelations(recipe.getId(), toCreateRecipeDTO(dto));
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
        replaceRecipeRelations(recipe.getId(), dto);
        return recipe.getId();
    }

    @Override
    public List<RecipeVO> getPendingRecipes() {
        return this.list(new LambdaQueryWrapper<Recipe>()
                        .eq(Recipe::getStatus, "pending")
                        .orderByDesc(Recipe::getCreatedAt))
                .stream()
                .map(recipe -> convertToRecipeVO(recipe, getRecipeTags(recipe.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<RecipeVO> getAdminRecipeList(String status, String category, String keyword) {
        LambdaQueryWrapper<Recipe> wrapper = new LambdaQueryWrapper<>();

        if (status != null && !status.isBlank() && !status.equals("全部")) {
            wrapper.eq(Recipe::getStatus, status.trim());
        }
        if (category != null && !category.isBlank() && !category.equals("全部")) {
            wrapper.eq(Recipe::getCategory, category.trim());
        }
        if (keyword != null && !keyword.isBlank()) {
            String trimmedKeyword = keyword.trim();
            wrapper.and(w -> w.like(Recipe::getName, trimmedKeyword)
                    .or()
                    .like(Recipe::getDescription, trimmedKeyword));
        }

        wrapper.orderByDesc(Recipe::getCreatedAt)
                .orderByDesc(Recipe::getId);

        return this.list(wrapper).stream()
                .map(recipe -> convertToRecipeVO(recipe, getRecipeTags(recipe.getId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveRecipe(Integer id) {
        Recipe recipe = requireRecipe(id);
        recipe.setStatus("approved");
        recipe.setRejectReason(null);
        this.updateById(recipe);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectRecipe(Integer id, String reason) {
        Recipe recipe = requireRecipe(id);
        recipe.setStatus("rejected");
        recipe.setRejectReason(reason);
        this.updateById(recipe);
    }

    @Override
    public List<RecipeVO> getUserRecipes(Integer userId) {
        return this.list(new LambdaQueryWrapper<Recipe>()
                        .eq(Recipe::getUserId, userId)
                        .orderByDesc(Recipe::getCreatedAt))
                .stream()
                .map(recipe -> convertToRecipeVO(recipe, getRecipeTags(recipe.getId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRecipe(Integer id, CreateRecipeDTO dto, Integer userId) {
        Recipe recipe = requireRecipe(id);
        if (recipe.getUserId() == null || !recipe.getUserId().equals(userId)) {
            throw new BusinessException("无权修改该食谱");
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
        replaceRecipeRelations(recipe.getId(), dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminUpdateRecipe(Integer id, CreateRecipeDTO dto) {
        Recipe recipe = requireRecipe(id);
        recipe.setName(dto.getName());
        recipe.setCategory(dto.getCategory());
        recipe.setImage(dto.getImage());
        recipe.setDescription(dto.getDescription());
        recipe.setCalories(dto.getCalories());
        recipe.setProtein(dto.getProtein());
        recipe.setCarbs(dto.getCarbs());
        recipe.setFat(dto.getFat());
        recipe.setStatus("approved");
        recipe.setRejectReason(null);
        this.updateById(recipe);
        replaceRecipeRelations(recipe.getId(), dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminDeleteRecipe(Integer id) {
        requireRecipe(id);
        this.removeById(id);
    }

    private Set<Integer> findRecipeIdsByTagAndGoal(String tag, String goal) {
        Set<Integer> ids = null;

        if (tag != null && !tag.isBlank()) {
            ids = recipeTagMapper.selectList(new LambdaQueryWrapper<RecipeTag>()
                            .eq(RecipeTag::getTag, tag.trim()))
                    .stream()
                    .map(RecipeTag::getRecipeId)
                    .collect(Collectors.toCollection(HashSet::new));
        }

        if (goal != null && !goal.isBlank()) {
            Set<Integer> goalIds = recipeSuitableGoalMapper.selectList(
                            new LambdaQueryWrapper<RecipeSuitableGoal>()
                                    .eq(RecipeSuitableGoal::getGoal, goal.trim()))
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

    private Recipe requireRecipe(Integer id) {
        Recipe recipe = this.getById(id);
        if (recipe == null) {
            throw new BusinessException("食谱不存在");
        }
        return recipe;
    }

    private void replaceRecipeRelations(Integer recipeId, CreateRecipeDTO dto) {
        recipeIngredientMapper.delete(new LambdaQueryWrapper<RecipeIngredient>()
                .eq(RecipeIngredient::getRecipeId, recipeId));
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

        recipeTagMapper.delete(new LambdaQueryWrapper<RecipeTag>()
                .eq(RecipeTag::getRecipeId, recipeId));
        if (dto.getTags() != null) {
            for (String tag : dto.getTags()) {
                RecipeTag recipeTag = new RecipeTag();
                recipeTag.setRecipeId(recipeId);
                recipeTag.setTag(tag);
                recipeTagMapper.insert(recipeTag);
            }
        }

        recipeSuitableGoalMapper.delete(new LambdaQueryWrapper<RecipeSuitableGoal>()
                .eq(RecipeSuitableGoal::getRecipeId, recipeId));
        if (dto.getSuitableGoals() != null) {
            for (String goal : dto.getSuitableGoals()) {
                RecipeSuitableGoal suitableGoal = new RecipeSuitableGoal();
                suitableGoal.setRecipeId(recipeId);
                suitableGoal.setGoal(goal);
                recipeSuitableGoalMapper.insert(suitableGoal);
            }
        }

        recipeStepMapper.delete(new LambdaQueryWrapper<RecipeStep>()
                .eq(RecipeStep::getRecipeId, recipeId));
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

    private CreateRecipeDTO toCreateRecipeDTO(RecipeImportDTO dto) {
        CreateRecipeDTO createRecipeDTO = new CreateRecipeDTO();
        createRecipeDTO.setName(dto.getName());
        createRecipeDTO.setCategory(dto.getCategory());
        createRecipeDTO.setImage(dto.getImage());
        createRecipeDTO.setDescription(dto.getDescription());
        createRecipeDTO.setCalories(dto.getCalories());
        createRecipeDTO.setProtein(dto.getProtein());
        createRecipeDTO.setCarbs(dto.getCarbs());
        createRecipeDTO.setFat(dto.getFat());
        createRecipeDTO.setIngredients(dto.getIngredients());
        createRecipeDTO.setTags(dto.getTags());
        createRecipeDTO.setSuitableGoals(dto.getSuitableGoals());
        createRecipeDTO.setSteps(dto.getSteps());
        return createRecipeDTO;
    }
}
