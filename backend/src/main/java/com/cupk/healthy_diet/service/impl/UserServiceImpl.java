package com.cupk.healthy_diet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cupk.healthy_diet.config.WechatConfig;
import com.cupk.healthy_diet.dto.LoginRequest;
import com.cupk.healthy_diet.dto.RegisterRequest;
import com.cupk.healthy_diet.dto.WechatLoginRequest;
import com.cupk.healthy_diet.entity.User;
import com.cupk.healthy_diet.exception.BusinessException;
import com.cupk.healthy_diet.mapper.UserMapper;
import com.cupk.healthy_diet.security.AuthTokenManager;
import com.cupk.healthy_diet.service.UserService;
import com.cupk.healthy_diet.vo.UserVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final WechatConfig wechatConfig;
    private final AuthTokenManager authTokenManager;
    private final OkHttpClient okHttpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public UserVO register(RegisterRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        User existUser = this.getOne(wrapper);
        
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("user");
        this.save(user);

        return toUserVO(user);
    }

    @Override
    public UserVO login(LoginRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        User user = this.getOne(wrapper);
        
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        return toUserVO(user);
    }

    @Override
    public UserVO wechatLogin(WechatLoginRequest request) {
        String openid = getWechatOpenid(request.getCode());

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOpenid, openid);
        User user = this.getOne(wrapper);

        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setNickname(request.getNickname());
            user.setAvatarUrl(request.getAvatarUrl());
            user.setRole("user");
            this.save(user);
        } else {
            user.setNickname(request.getNickname());
            user.setAvatarUrl(request.getAvatarUrl());
            this.updateById(user);
        }

        return toUserVO(user);
    }

    private UserVO toUserVO(User user) {
        String token = authTokenManager.createToken(user.getId(), user.getRole());
        return new UserVO(user.getId(), user.getUsername(), user.getNickname(), user.getAvatarUrl(), user.getRole(), token);
    }

    private String getWechatOpenid(String code) {
        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                wechatConfig.getAppId(),
                wechatConfig.getAppSecret(),
                code
        );

        Request request = new Request.Builder().url(url).build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new BusinessException("微信登录失败");
            }

            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            if (jsonNode.has("errcode")) {
                throw new BusinessException("微信登录失败: " + jsonNode.get("errmsg").asText());
            }

            return jsonNode.get("openid").asText();
        } catch (IOException e) {
            throw new BusinessException("微信登录网络异常");
        }
    }
}
