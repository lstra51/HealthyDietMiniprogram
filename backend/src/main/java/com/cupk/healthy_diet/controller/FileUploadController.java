package com.cupk.healthy_diet.controller;

import com.cupk.healthy_diet.common.Result;
import com.cupk.healthy_diet.config.UploadStorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp");

    private final UploadStorageProperties uploadStorageProperties;

    @Value("${file.upload.max-size-bytes:${FILE_UPLOAD_MAX_SIZE_BYTES:5242880}}")
    private long maxSizeBytes;

    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("只支持图片文件");
        }
        if (file.getSize() > maxSizeBytes) {
            return Result.error("图片大小不能超过 " + (maxSizeBytes / 1024 / 1024) + "MB");
        }

        try {
            String datePath = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
            Path path = uploadStorageProperties.getUploadRoot().resolve(datePath).normalize();
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = getSafeExtension(originalFilename);
            String newFilename = UUID.randomUUID().toString() + extension;
            Path targetPath = path.resolve(newFilename).normalize();
            file.transferTo(targetPath);

            String fileUrl = "/uploads/" + datePath + "/" + newFilename;
            
            Map<String, String> result = new HashMap<>();
            result.put("url", uploadStorageProperties.buildPublicUrl(fileUrl));
            result.put("relativeUrl", fileUrl);
            result.put("filename", newFilename);

            return Result.success(result);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (IOException e) {
            return Result.error("文件上传失败，请稍后重试");
        }
    }

    private String getSafeExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return ".jpg";
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("不支持的图片格式");
        }
        return extension;
    }
}
