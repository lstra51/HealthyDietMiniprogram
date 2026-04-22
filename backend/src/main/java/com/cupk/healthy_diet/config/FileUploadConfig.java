package com.cupk.healthy_diet.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取项目根目录的绝对路径
        String projectRoot = System.getProperty("user.dir");
        String absolutePath = projectRoot + File.separator + uploadPath + File.separator;
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + absolutePath);
    }
}
