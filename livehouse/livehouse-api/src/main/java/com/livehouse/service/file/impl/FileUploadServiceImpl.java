package com.livehouse.service.file.impl;

import com.livehouse.common.exception.BusinessException;
import com.livehouse.config.FileUploadProperties;
import com.livehouse.service.file.FileUploadService;
import com.livehouse.service.file.FileUploadStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传服务实现 - 根据配置动态选择上传策略
 */
@Slf4j
@Service
public class FileUploadServiceImpl implements FileUploadService {

    @Autowired
    private FileUploadProperties properties;

    @Autowired
    private LocalFileUploadStrategy localStrategy;

    @Autowired(required = false)
    private AliyunFileUploadStrategy aliyunStrategy;

    @Autowired(required = false)
    private TencentFileUploadStrategy tencentStrategy;

    private FileUploadStrategy currentStrategy;

    @PostConstruct
    public void init() {
        refreshStrategy();
    }

    /**
     * 刷新当前策略
     */
    private void refreshStrategy() {
        String type = properties.getUpload().getType();
        log.info("初始化文件上传策略: {}", type);
        switch (type) {
            case "aliyun":
                if (aliyunStrategy == null) {
                    log.warn("阿里云OSS策略未启用（file.aliyun.oss.enabled=false），回退到本地存储");
                    currentStrategy = localStrategy;
                } else {
                    currentStrategy = aliyunStrategy;
                }
                break;
            case "tencent":
                if (tencentStrategy == null) {
                    log.warn("腾讯云COS策略未启用（file.tencent.cos.enabled=false），回退到本地存储");
                    currentStrategy = localStrategy;
                } else {
                    currentStrategy = tencentStrategy;
                }
                break;
            case "local":
            default:
                currentStrategy = localStrategy;
                break;
        }
        log.info("当前文件上传策略: {}", currentStrategy.getType());
    }

    @Override
    public String uploadFile(MultipartFile file, String bizType) {
        validateFile(file);
        return currentStrategy.upload(file, bizType);
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            return false;
        }
        return currentStrategy.delete(fileUrl);
    }

    @Override
    public String getStorageType() {
        return currentStrategy.getType();
    }

    @Override
    public Map<String, Object> uploadWithResult(MultipartFile file, String bizType) {
        String url = uploadFile(file, bizType);
        Map<String, Object> result = new HashMap<>();
        result.put("url", url);
        result.put("name", file.getOriginalFilename());
        result.put("size", file.getSize());
        result.put("contentType", file.getContentType());
        result.put("storageType", currentStrategy.getType());
        return result;
    }

    /**
     * 校验文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件为空");
        }

        // 大小校验
        long maxSize = properties.getUpload().getMaxSize();
        if (file.getSize() > maxSize) {
            throw new BusinessException("文件大小超过限制，最大允许: " + (maxSize / 1024 / 1024) + "MB");
        }

        // 后缀校验
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            throw new BusinessException("文件名为空");
        }
        String ext = getFileExtension(originalFilename);
        if (ext == null) {
            throw new BusinessException("无法识别文件类型");
        }
        String allowedTypes = properties.getUpload().getAllowedTypes();
        if (StringUtils.hasText(allowedTypes)) {
            String[] types = allowedTypes.split(",");
            boolean allowed = false;
            for (String type : types) {
                if (type.trim().equalsIgnoreCase(ext)) {
                    allowed = true;
                    break;
                }
            }
            if (!allowed) {
                throw new BusinessException("不支持的文件类型: " + ext);
            }
        }
    }

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
