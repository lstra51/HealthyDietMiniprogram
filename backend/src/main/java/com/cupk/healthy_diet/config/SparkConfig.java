package com.cupk.healthy_diet.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spark")
@Data
public class SparkConfig {
    private String appId;
    private String apiSecret;
    private String apiKey;
    private String apiPassword;
    private String host;
    private String path;
    private String domain;
    private String httpUrl;
    private String model;
}
