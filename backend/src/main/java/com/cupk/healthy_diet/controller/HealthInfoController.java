package com.cupk.healthy_diet.controller;

import com.cupk.healthy_diet.common.Result;
import com.cupk.healthy_diet.dto.HealthInfoRequest;
import com.cupk.healthy_diet.entity.HealthInfo;
import com.cupk.healthy_diet.exception.BusinessException;
import com.cupk.healthy_diet.security.AuthContext;
import com.cupk.healthy_diet.service.HealthInfoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthInfoController {

    private final HealthInfoService healthInfoService;

    @PostMapping
    public Result<HealthInfo> saveHealthInfo(@Valid @RequestBody HealthInfoRequest request,
                                             @RequestAttribute(AuthContext.USER_ID) Integer userId) {
        request.setUserId(userId);
        HealthInfo healthInfo = healthInfoService.saveOrUpdateHealthInfo(request);
        return Result.success("保存成功", healthInfo);
    }

    @GetMapping("/{userId}")
    public Result<HealthInfo> getHealthInfo(@PathVariable Integer userId,
                                            @RequestAttribute(AuthContext.USER_ID) Integer currentUserId) {
        if (!userId.equals(currentUserId)) {
            throw new BusinessException(403, "无权访问其他用户的数据");
        }
        HealthInfo healthInfo = healthInfoService.getHealthInfoByUserId(userId);
        return Result.success(healthInfo);
    }
}
