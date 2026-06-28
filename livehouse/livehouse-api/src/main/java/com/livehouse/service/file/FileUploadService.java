package com.livehouse.service.file;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 文件上传服务
 */
public interface FileUploadService {

    /**
     * 上传文件
     *
     * @param file 文件
     * @param bizType 业务类型
     * @return 访问URL
     */
    String uploadFile(MultipartFile file, String bizType);

    /**
     * 删除文件
     *
     * @param fileUrl 文件URL
     * @return 是否成功
     */
    boolean deleteFile(String fileUrl);

    /**
     * 获取当前使用的存储类型
     *
     * @return 存储类型
     */
    String getStorageType();

    /**
     * 获取上传结果（含完整信息）
     *
     * @param file 文件
     * @param bizType 业务类型
     * @return 上传结果
     */
    Map<String, Object> uploadWithResult(MultipartFile file, String bizType);
}
