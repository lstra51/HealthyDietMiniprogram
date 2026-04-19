package com.cupk.healthy_diet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cupk.healthy_diet.dto.HealthInfoRequest;
import com.cupk.healthy_diet.entity.HealthInfo;

public interface HealthInfoService extends IService<HealthInfo> {
    HealthInfo saveOrUpdateHealthInfo(HealthInfoRequest request);
    HealthInfo getHealthInfoByUserId(Integer userId);
}
