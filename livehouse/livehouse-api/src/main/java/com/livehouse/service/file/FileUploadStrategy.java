package com.livehouse.service.file;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Map;

/**
 * 文件上传策略接口
 */
public interface FileUploadStrategy {

    /**
     * 上传文件
     *
     * @param file 文件
     * @param bizType 业务类型（用于子目录，如 image/video）
     * @return 访问URL
     */
    String upload(MultipartFile file, String bizType);

    /**
     * 上传文件（字节流）
     *
     * @param inputStream 文件输入流
     * @param fileName 文件名
     * @param bizType 业务类型
     * @return 访问URL
     */
    String upload(InputStream inputStream, String fileName, String bizType);

    /**
     * 删除文件
     *
     * @param fileUrl 文件URL
     * @return 是否删除成功
     */
    boolean delete(String fileUrl);

    /**
     * 获取存储类型
     *
     * @return 存储类型：local/aliyun/tencent
     */
    String getType();
}
