package com.livehouse.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文件上传配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "file")
public class FileUploadProperties {

    /**
     * 通用上传配置
     */
    private Upload upload = new Upload();

    /**
     * 阿里云OSS配置
     */
    private Aliyun aliyun = new Aliyun();

    /**
     * 腾讯云COS配置
     */
    private Tencent tencent = new Tencent();

    @Data
    public static class Upload {
        /**
         * 上传类型：local-本地存储 / aliyun-阿里云OSS / tencent-腾讯云COS
         */
        private String type = "local";

        /**
         * 本地存储配置
         */
        private Local local = new Local();

        /**
         * 允许上传的文件类型（后缀）
         */
        private String allowedTypes = "jpg,jpeg,png,gif,bmp,webp,mp4,webm";

        /**
         * 单个文件最大大小（字节）
         */
        private Long maxSize = 52428800L;

        /**
         * 文件访问URL前缀（用于拼接）
         */
        private String urlPrefix = "";
    }

    @Data
    public static class Local {
        /**
         * 本地存储路径
         */
        private String path = "./uploads";

        /**
         * 本地访问URL前缀
         */
        private String urlPrefix = "/uploads/";
    }

    @Data
    public static class Aliyun {
        private Oss oss = new Oss();

        @Data
        public static class Oss {
            private boolean enabled = false;
            private String endpoint = "";
            private String accessKeyId = "";
            private String accessKeySecret = "";
            private String bucketName = "";
            private String urlPrefix = "";
        }
    }

    @Data
    public static class Tencent {
        private Cos cos = new Cos();

        @Data
        public static class Cos {
            private boolean enabled = false;
            private String region = "";
            private String secretId = "";
            private String secretKey = "";
            private String bucketName = "";
            private String appId = "";
            private String urlPrefix = "";
        }
    }
}
