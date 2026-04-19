package com.cupk.healthy_diet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cupk.healthy_diet.dto.LoginRequest;
import com.cupk.healthy_diet.dto.RegisterRequest;
import com.cupk.healthy_diet.dto.WechatLoginRequest;
import com.cupk.healthy_diet.entity.User;
import com.cupk.healthy_diet.vo.UserVO;

public interface UserService extends IService<User> {
    UserVO register(RegisterRequest request);
    UserVO login(LoginRequest request);
    UserVO wechatLogin(WechatLoginRequest request);
}
