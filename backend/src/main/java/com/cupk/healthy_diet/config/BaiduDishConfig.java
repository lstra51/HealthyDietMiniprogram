package com.cupk.healthy_diet.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class BaiduDishConfig {

    @Value("${baidu.dish.app-id:}")
    private String appId;

    @Value("${baidu.dish.api-key:}")
    private String apiKey;

    @Value("${baidu.dish.secret-key:}")
    private String secretKey;

    @Value("${baidu.dish.token-url:https://aip.baidubce.com/oauth/2.0/token}")
    private String tokenUrl;

    @Value("${baidu.dish.dish-url:https://aip.baidubce.com/rest/2.0/image-classify/v2/dish}")
    private String dishUrl;
}
