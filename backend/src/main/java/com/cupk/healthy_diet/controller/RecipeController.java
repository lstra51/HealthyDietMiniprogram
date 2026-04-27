package com.cupk.healthy_diet.controller;

import com.cupk.healthy_diet.common.Result;
import com.cupk.healthy_diet.dto.CreateRecipeDTO;
import com.cupk.healthy_diet.dto.RecipeImportDTO;
import com.cupk.healthy_diet.dto.RejectRecipeDTO;
import com.cupk.healthy_diet.exception.BusinessException;
import com.cupk.healthy_diet.security.AuthContext;
import com.cupk.healthy_diet.service.RecipeService;
import com.cupk.healthy_diet.vo.RecipeDetailVO;
import com.cupk.healthy_diet.vo.RecipeVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;

    @GetMapping
    public Result<List<RecipeVO>> getRecipeList(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer minCalories,
            @RequestParam(required = false) Integer maxCalories,
            @RequestParam(required = false) Double minProtein,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String goal) {
        List<RecipeVO> recipes = recipeService.getRecipeList(
                category, keyword, minCalories, maxCalories, minProtein, tag, goal);
        return Result.success(recipes);
    }

    @GetMapping("/tags")
    public Result<List<String>> getAvailableTags() {
        return Result.success(recipeService.getAvailableTags());
    }

    @GetMapping("/admin")
    public Result<List<RecipeVO>> getAdminRecipeList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestAttribute(AuthContext.USER_ROLE) String role) {
        requireAdmin(role);
        return Result.success(recipeService.getAdminRecipeList(status, category, keyword));
    }

    @GetMapping("/{id}")
    public Result<RecipeDetailVO> getRecipeDetail(@PathVariable Integer id) {
        return Result.success(recipeService.getRecipeDetail(id));
    }

    @PostMapping("/import")
    public Result<Void> importRecipes(@Valid @RequestBody List<RecipeImportDTO> recipeDTOs,
                                      @RequestAttribute(AuthContext.USER_ROLE) String role) {
        requireAdmin(role);
        recipeService.importRecipes(recipeDTOs);
        return Result.success();
    }

    @PostMapping
    public Result<Map<String, Object>> createRecipe(@Valid @RequestBody CreateRecipeDTO dto,
                                                    @RequestAttribute(AuthContext.USER_ID) Integer userId) {
        Integer recipeId = recipeService.createRecipe(dto, userId);
        Map<String, Object> result = new HashMap<>();
        result.put("id", recipeId);
        result.put("message", "食谱已提交，等待审核");
        return Result.success(result);
    }

    @GetMapping("/pending")
    public Result<List<RecipeVO>> getPendingRecipes(@RequestAttribute(AuthContext.USER_ROLE) String role) {
        requireAdmin(role);
        return Result.success(recipeService.getPendingRecipes());
    }

    @PutMapping("/{id}/approve")
    public Result<Void> approveRecipe(@PathVariable Integer id,
                                      @RequestAttribute(AuthContext.USER_ROLE) String role) {
        requireAdmin(role);
        recipeService.approveRecipe(id);
        return Result.success();
    }

    @PutMapping("/{id}/reject")
    public Result<Void> rejectRecipe(@PathVariable Integer id,
                                     @Valid @RequestBody RejectRecipeDTO dto,
                                     @RequestAttribute(AuthContext.USER_ROLE) String role) {
        requireAdmin(role);
        recipeService.rejectRecipe(id, dto.getReason());
        return Result.success();
    }

    @GetMapping("/user")
    public Result<List<RecipeVO>> getUserRecipes(@RequestAttribute(AuthContext.USER_ID) Integer userId) {
        return Result.success(recipeService.getUserRecipes(userId));
    }

    @PutMapping("/{id}")
    public Result<Void> updateRecipe(@PathVariable Integer id,
                                     @Valid @RequestBody CreateRecipeDTO dto,
                                     @RequestAttribute(AuthContext.USER_ID) Integer userId) {
        recipeService.updateRecipe(id, dto, userId);
        return Result.success();
    }

    @PutMapping("/{id}/admin")
    public Result<Void> adminUpdateRecipe(@PathVariable Integer id,
                                          @Valid @RequestBody CreateRecipeDTO dto,
                                          @RequestAttribute(AuthContext.USER_ROLE) String role) {
        requireAdmin(role);
        recipeService.adminUpdateRecipe(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}/admin")
    public Result<Void> adminDeleteRecipe(@PathVariable Integer id,
                                          @RequestAttribute(AuthContext.USER_ROLE) String role) {
        requireAdmin(role);
        recipeService.adminDeleteRecipe(id);
        return Result.success();
    }

    private void requireAdmin(String role) {
        if (!AuthContext.isAdmin(role)) {
            throw new BusinessException(403, "无权限访问");
        }
    }
}
