package com.livehouse.service.file.impl;

import com.livehouse.config.FileUploadProperties;
import com.livehouse.service.file.FileUploadStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * 本地文件上传实现
 */
@Slf4j
@Service("localFileUploadStrategy")
public class LocalFileUploadStrategy implements FileUploadStrategy {

    @Autowired
    private FileUploadProperties properties;

    private String basePath;
    private String urlPrefix;

    @PostConstruct
    public void init() {
        this.basePath = properties.getUpload().getLocal().getPath();
        this.urlPrefix = properties.getUpload().getLocal().getUrlPrefix();
        // 确保目录存在
        File dir = new File(basePath);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                log.info("创建上传目录: {}", basePath);
            }
        }
    }

    @Override
    public String upload(MultipartFile file, String bizType) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }
        try {
            return upload(file.getInputStream(), file.getOriginalFilename(), bizType);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public String upload(InputStream inputStream, String fileName, String bizType) {
        if (inputStream == null) {
            throw new IllegalArgumentException("文件流为空");
        }
        try {
            // 生成相对路径 bizType/yyyyMM/xxx.ext
            String dateDir = new SimpleDateFormat("yyyyMMdd").format(new Date());
            String bizDir = StringUtils.hasText(bizType) ? bizType + "/" : "";
            String relativePath = bizDir + dateDir;

            // 生成唯一文件名
            String ext = getFileExtension(fileName);
            String uniqueName = UUID.randomUUID().toString().replace("-", "") + (ext != null ? "." + ext : "");

            // 完整路径
            String relativeFilePath = relativePath + "/" + uniqueName;
            Path targetPath = Paths.get(basePath, relativeFilePath);

            // 确保目录存在
            File parentDir = targetPath.getParent().toFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }

            // 写入文件
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);

            // 返回访问URL
            String url = urlPrefix.endsWith("/") ? urlPrefix + relativeFilePath : urlPrefix + "/" + relativeFilePath;
            log.info("文件上传成功: {}", url);
            return url;
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public boolean delete(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            return false;
        }
        try {
            // 从URL中提取相对路径
            String relativePath = fileUrl;
            if (fileUrl.startsWith(urlPrefix)) {
                relativePath = fileUrl.substring(urlPrefix.length());
            }
            Path filePath = Paths.get(basePath, relativePath);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("文件删除失败: {}", fileUrl, e);
            return false;
        }
    }

    @Override
    public String getType() {
        return "local";
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null) {
            return null;
        }
        int idx = fileName.lastIndexOf(".");
        if (idx == -1) {
            return null;
        }
        return fileName.substring(idx + 1);
    }
}
