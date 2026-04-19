package com.cupk.healthy_diet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cupk.healthy_diet.dto.HealthInfoRequest;
import com.cupk.healthy_diet.entity.HealthInfo;
import com.cupk.healthy_diet.mapper.HealthInfoMapper;
import com.cupk.healthy_diet.service.HealthInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HealthInfoServiceImpl extends ServiceImpl<HealthInfoMapper, HealthInfo> implements HealthInfoService {

    @Override
    public HealthInfo saveOrUpdateHealthInfo(HealthInfoRequest request) {
        LambdaQueryWrapper<HealthInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthInfo::getUserId, request.getUserId());
        HealthInfo existInfo = this.getOne(wrapper);

        HealthInfo healthInfo;
        if (existInfo != null) {
            healthInfo = existInfo;
        } else {
            healthInfo = new HealthInfo();
            healthInfo.setUserId(request.getUserId());
        }

        healthInfo.setHeight(request.getHeight());
        healthInfo.setWeight(request.getWeight());
        healthInfo.setGender(request.getGender());
        healthInfo.setGoal(request.getGoal());

        if (existInfo != null) {
            this.updateById(healthInfo);
        } else {
            this.save(healthInfo);
        }

        return healthInfo;
    }

    @Override
    public HealthInfo getHealthInfoByUserId(Integer userId) {
        LambdaQueryWrapper<HealthInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthInfo::getUserId, userId);
        return this.getOne(wrapper);
    }
}
