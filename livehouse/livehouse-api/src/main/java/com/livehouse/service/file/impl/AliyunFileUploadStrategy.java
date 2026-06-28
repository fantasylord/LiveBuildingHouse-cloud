package com.livehouse.service.file.impl;

import com.livehouse.config.FileUploadProperties;
import com.livehouse.service.file.FileUploadStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * 阿里云OSS文件上传实现（仅作示例代码，需要引入aliyun-sdk-oss后启用）
 *
 * 启用方式：
 * 1. 在 pom.xml 中添加 aliyun-sdk-oss 依赖
 * 2. 设置 file.upload.type=aliyun
 * 3. 完善下方代码中的 OSS 调用
 */
@Slf4j
@Service("aliyunFileUploadStrategy")
@ConditionalOnProperty(prefix = "file.aliyun.oss", name = "enabled", havingValue = "true")
public class AliyunFileUploadStrategy implements FileUploadStrategy {

    @Autowired
    private FileUploadProperties properties;

    @Override
    public String upload(MultipartFile file, String bizType) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }
        try {
            return upload(file.getInputStream(), file.getOriginalFilename(), bizType);
        } catch (Exception e) {
            log.error("阿里云OSS文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public String upload(InputStream inputStream, String fileName, String bizType) {
        FileUploadProperties.Aliyun.Oss oss = properties.getAliyun().getOss();
        // 生成OSS存储路径
        String dateDir = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String bizDir = StringUtils.hasText(bizType) ? bizType + "/" : "";
        String ext = getFileExtension(fileName);
        String objectName = bizDir + dateDir + "/" + UUID.randomUUID().toString().replace("-", "")
                + (ext != null ? "." + ext : "");

        log.info("阿里云OSS上传 - endpoint:{}, bucket:{}, objectName:{}", oss.getEndpoint(), oss.getBucketName(), objectName);

        // TODO 引入 aliyun-sdk-oss 后替换为真实上传逻辑
        // OSS ossClient = new OSS(oss.getEndpoint(), oss.getAccessKeyId(), oss.getAccessKeySecret());
        // ossClient.putObject(oss.getBucketName(), objectName, inputStream);
        // ossClient.shutdown();

        // 临时返回URL前缀 + 路径
        String urlPrefix = StringUtils.hasText(oss.getUrlPrefix()) ? oss.getUrlPrefix() : "https://" + oss.getBucketName() + "." + oss.getEndpoint() + "/";
        return urlPrefix + objectName;
    }

    @Override
    public boolean delete(String fileUrl) {
        log.info("阿里云OSS删除文件: {}", fileUrl);
        // TODO 引入 aliyun-sdk-oss 后替换为真实删除逻辑
        return true;
    }

    @Override
    public String getType() {
        return "aliyun";
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
