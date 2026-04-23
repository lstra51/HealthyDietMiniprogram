package com.cupk.healthy_diet.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class UploadStorageProperties {

    @Value("${file.upload.path:${FILE_UPLOAD_PATH:uploads}}")
    private String uploadPath;

    @Value("${file.upload.base-url:${FILE_UPLOAD_BASE_URL:https://health.cupk.space}}")
    private String baseUrl;

    public Path getUploadRoot() {
        Path configured = Paths.get(uploadPath);
        if (configured.isAbsolute()) {
            return configured.normalize();
        }
        return Paths.get(System.getProperty("user.dir")).resolve(configured).normalize();
    }

    public String buildPublicUrl(String relativeUrl) {
        String normalizedBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String normalizedRelative = relativeUrl.startsWith("/") ? relativeUrl : "/" + relativeUrl;
        return normalizedBase + normalizedRelative;
    }
}
