package com.livehouse.controller.common;

import com.livehouse.common.exception.BusinessException;
import com.livehouse.common.result.Result;
import com.livehouse.config.FileUploadProperties;
import com.livehouse.service.file.FileUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 文件上传控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/common/upload")
@CrossOrigin(origins = "*", maxAge = 3600)
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private FileUploadProperties properties;

    /**
     * 通用文件上传
     *
     * @param file 文件
     * @param bizType 业务类型（image/video/vr/cover）
     * @return 访问URL
     */
    @PostMapping("/file")
    public Result<Map<String, Object>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "bizType", defaultValue = "common") String bizType) {
        try {
            Map<String, Object> result = fileUploadService.uploadWithResult(file, bizType);
            return Result.success("上传成功", result);
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 图片上传（用于VR全景图、缩略图、楼盘图片等）
     */
    @PostMapping("/image")
    public Result<Map<String, Object>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "bizType", defaultValue = "image") String bizType) {
        try {
            Map<String, Object> result = fileUploadService.uploadWithResult(file, bizType);
            return Result.success("上传成功", result);
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("图片上传失败", e);
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }

    /**
     * 视频上传
     */
    @PostMapping("/video")
    public Result<Map<String, Object>> uploadVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "bizType", defaultValue = "video") String bizType) {
        try {
            Map<String, Object> result = fileUploadService.uploadWithResult(file, bizType);
            return Result.success("上传成功", result);
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("视频上传失败", e);
            return Result.error("视频上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/file")
    public Result<Boolean> deleteFile(@RequestParam("url") String url) {
        try {
            boolean success = fileUploadService.deleteFile(url);
            return success ? Result.success("删除成功", true) : Result.error("删除失败");
        } catch (Exception e) {
            log.error("文件删除失败", e);
            return Result.error("文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取上传配置信息
     */
    @GetMapping("/config")
    public Result<Map<String, Object>> getConfig() {
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("storageType", fileUploadService.getStorageType());
        result.put("maxSize", properties.getUpload().getMaxSize());
        result.put("allowedTypes", properties.getUpload().getAllowedTypes());
        return Result.success(result);
    }
}
