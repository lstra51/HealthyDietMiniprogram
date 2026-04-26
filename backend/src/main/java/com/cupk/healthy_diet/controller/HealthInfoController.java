package com.cupk.healthy_diet.controller;

import com.cupk.healthy_diet.common.Result;
import com.cupk.healthy_diet.dto.HealthInfoRequest;
import com.cupk.healthy_diet.entity.HealthInfo;
import com.cupk.healthy_diet.exception.BusinessException;
import com.cupk.healthy_diet.security.AuthContext;
import com.cupk.healthy_diet.service.HealthInfoService;
import com.cupk.healthy_diet.vo.HealthInfoVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthInfoController {

    private final HealthInfoService healthInfoService;

    @PostMapping
    public Result<HealthInfoVO> saveHealthInfo(@Valid @RequestBody HealthInfoRequest request,
                                               @RequestAttribute(AuthContext.USER_ID) Integer userId) {
        request.setUserId(userId);
        HealthInfo healthInfo = healthInfoService.saveOrUpdateHealthInfo(request);
        return Result.success("保存成功", convertToVO(healthInfo));
    }

    @GetMapping("/{userId}")
    public Result<HealthInfoVO> getHealthInfo(@PathVariable Integer userId,
                                              @RequestAttribute(AuthContext.USER_ID) Integer currentUserId) {
        if (!userId.equals(currentUserId)) {
            throw new BusinessException(403, "无权访问其他用户的数据");
        }
        HealthInfo healthInfo = healthInfoService.getHealthInfoByUserId(userId);
        return Result.success(convertToVO(healthInfo));
    }

    private HealthInfoVO convertToVO(HealthInfo healthInfo) {
        if (healthInfo == null) {
            return null;
        }
        return new HealthInfoVO(
                healthInfo.getId(),
                healthInfo.getUserId(),
                healthInfo.getHeight(),
                healthInfo.getWeight(),
                healthInfo.getGender(),
                healthInfo.getGoal(),
                splitPreferences(healthInfo.getDietaryPreferences()),
                healthInfo.getCreatedAt(),
                healthInfo.getUpdatedAt()
        );
    }

    private List<String> splitPreferences(String preferences) {
        if (preferences == null || preferences.isBlank()) {
            return List.of();
        }
        return Arrays.stream(preferences.split(","))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .toList();
    }
}
