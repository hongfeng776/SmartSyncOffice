package com.smartsync.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "file-upload")
public class FileUploadConfig {

    private Long maxFileSize = 524288000L;

    private List<String> allowedExtensions;

    private List<String> forbiddenExtensions;
}
