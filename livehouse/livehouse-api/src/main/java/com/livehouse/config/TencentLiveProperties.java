package com.livehouse.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 腾讯云直播配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "tencent.cloud")
public class TencentLiveProperties {

    /**
     * 腾讯云SecretId
     */
    private String secretId = "";

    /**
     * 腾讯云SecretKey
     */
    private String secretKey = "";

    /**
     * 直播服务区域
     */
    private String liveRegion = "ap-shanghai";

    /**
     * 推流域名
     */
    private String pushDomain = "";

    /**
     * 播放域名
     */
    private String playDomain = "";

    /**
     * 应用名称（通常为live）
     */
    private String appName = "live";

    /**
     * 推流地址有效期（秒）
     */
    private Integer pushExpireTime = 3600;

    /**
     * 播放地址有效期（秒）
     */
    private Integer playExpireTime = 86400;

    /**
     * 转码模板ID（可选，用于生成不同清晰度的播放地址）
     */
    private String transcodeTemplateId = "";

    /**
     * 是否启用防盗链签名
     */
    private boolean enableAntiLeech = true;

    /**
     * 是否启用
     */
    private boolean enabled = false;

    /**
     * 检查配置是否有效
     */
    public boolean isValid() {
        return enabled && 
               !secretId.isEmpty() && !secretId.startsWith("YOUR_") && 
               !secretKey.isEmpty() && !secretKey.startsWith("YOUR_");
    }
}