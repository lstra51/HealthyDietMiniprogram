package com.cupk.healthy_diet.controller;

import com.cupk.healthy_diet.common.Result;
import com.cupk.healthy_diet.dto.LoginRequest;
import com.cupk.healthy_diet.dto.RegisterRequest;
import com.cupk.healthy_diet.dto.WechatLoginRequest;
import com.cupk.healthy_diet.service.UserService;
import com.cupk.healthy_diet.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterRequest request) {
        UserVO userVO = userService.register(request);
        return Result.success("注册成功", userVO);
    }

    @PostMapping("/login")
    public Result<UserVO> login(@Valid @RequestBody LoginRequest request) {
        UserVO userVO = userService.login(request);
        return Result.success("登录成功", userVO);
    }

    @PostMapping("/wechat-login")
    public Result<UserVO> wechatLogin(@Valid @RequestBody WechatLoginRequest request) {
        UserVO userVO = userService.wechatLogin(request);
        return Result.success("登录成功", userVO);
    }
}
