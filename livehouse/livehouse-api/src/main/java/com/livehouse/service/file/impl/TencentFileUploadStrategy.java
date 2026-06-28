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
 * 腾讯云COS文件上传实现（仅作示例代码，需要引入cos_api后启用）
 *
 * 启用方式：
 * 1. 在 pom.xml 中添加 cos_api 依赖
 * 2. 设置 file.upload.type=tencent
 * 3. 完善下方代码中的 COS 调用
 */
@Slf4j
@Service("tencentFileUploadStrategy")
@ConditionalOnProperty(prefix = "file.tencent.cos", name = "enabled", havingValue = "true")
public class TencentFileUploadStrategy implements FileUploadStrategy {

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
            log.error("腾讯云COS文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public String upload(InputStream inputStream, String fileName, String bizType) {
        FileUploadProperties.Tencent.Cos cos = properties.getTencent().getCos();
        // 生成COS存储路径
        String dateDir = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String bizDir = StringUtils.hasText(bizType) ? bizType + "/" : "";
        String ext = getFileExtension(fileName);
        String key = bizDir + dateDir + "/" + UUID.randomUUID().toString().replace("-", "")
                + (ext != null ? "." + ext : "");

        log.info("腾讯云COS上传 - region:{}, bucket:{}, key:{}", cos.getRegion(), cos.getBucketName(), key);

        // TODO 引入 cos_api 后替换为真实上传逻辑
        // COSCredentials cred = new BasicCOSCredentials(cos.getSecretId(), cos.getSecretKey());
        // ClientConfig clientConfig = new ClientConfig(new Region(cos.getRegion()));
        // COSClient cosClient = new COSClient(cred, clientConfig);
        // ObjectMetadata metadata = new ObjectMetadata();
        // cosClient.putObject(cos.getBucketName(), key, inputStream, metadata);
        // cosClient.shutdown();

        // 临时返回URL前缀 + 路径
        String urlPrefix = StringUtils.hasText(cos.getUrlPrefix()) ? cos.getUrlPrefix()
                : "https://" + cos.getBucketName() + ".cos." + cos.getRegion() + ".myqcloud.com/";
        return urlPrefix + key;
    }

    @Override
    public boolean delete(String fileUrl) {
        log.info("腾讯云COS删除文件: {}", fileUrl);
        // TODO 引入 cos_api 后替换为真实删除逻辑
        return true;
    }

    @Override
    public String getType() {
        return "tencent";
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
