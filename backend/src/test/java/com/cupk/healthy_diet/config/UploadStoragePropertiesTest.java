package com.cupk.healthy_diet.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UploadStoragePropertiesTest {

    @TempDir
    Path tempDir;

    @Test
    void keepsAbsoluteUploadPath() {
        UploadStorageProperties properties = new UploadStorageProperties();
        ReflectionTestUtils.setField(properties, "uploadPath", tempDir.toString());
        ReflectionTestUtils.setField(properties, "baseUrl", "https://health.cupk.space/");

        assertEquals(tempDir.normalize(), properties.getUploadRoot());
        assertEquals("https://health.cupk.space/uploads/a.jpg", properties.buildPublicUrl("/uploads/a.jpg"));
    }

    @Test
    void resolvesRelativeUploadPathFromWorkingDirectory() {
        UploadStorageProperties properties = new UploadStorageProperties();
        ReflectionTestUtils.setField(properties, "uploadPath", "uploads");
        ReflectionTestUtils.setField(properties, "baseUrl", "https://health.cupk.space");

        Path expected = Path.of(System.getProperty("user.dir")).resolve("uploads").normalize();
        assertEquals(expected, properties.getUploadRoot());
    }
}
