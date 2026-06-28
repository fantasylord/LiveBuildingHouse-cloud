package com.livehouse.service.impl;

import com.livehouse.config.TencentLiveProperties;
import com.livehouse.entity.LiveAddress;
import com.livehouse.entity.LivePlatform;
import com.livehouse.service.LiveAddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * 统一直播地址生成服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LiveAddressServiceImpl implements LiveAddressService {

    private final TencentLiveProperties tencentLiveProperties;

    @Override
    public LiveAddress generateLiveAddress(LivePlatform platform, String streamId, Integer expireTime) {
        if (platform == null) {
            return null;
        }

        String effectiveStreamId = streamId != null ? streamId : generateStreamId();
        Integer effectiveExpireTime = expireTime != null ? expireTime : 
            (platform.getExpireTime() != null ? platform.getExpireTime() : 3600);

        switch (platform.getPlatformType()) {
            case "tencent":
                return generateTencentAddress(platform, effectiveStreamId, effectiveExpireTime);
            case "aliyun":
                return generateAliyunAddress(platform, effectiveStreamId, effectiveExpireTime);
            case "douyin":
                return generateDouyinAddress(platform, effectiveStreamId, effectiveExpireTime);
            default:
                log.warn("不支持的平台类型: {}", platform.getPlatformType());
                return null;
        }
    }

    @Override
    public String generateStreamId() {
        return "stream_" + System.currentTimeMillis() + "_" + 
            UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 生成腾讯云直播地址
     * 
     * 推流地址格式：rtmp://推流域名/应用名/流名?txSecret=xxx&txTime=xxx
     * 播放地址格式：http(s)://播放域名/应用名/流名.m3u8?txSecret=xxx&txTime=xxx
     * 
     * 签名算法：
     * txTime: 过期时间戳（十进制，单位秒）
     * txSecret: MD5(KEY+StreamName+txTime) 的十六进制字符串
     */
    private LiveAddress generateTencentAddress(LivePlatform platform, String streamId, Integer expireTime) {
        String pushDomain = getEffectivePushDomain(platform);
        String playDomain = getEffectivePlayDomain(platform);
        String appName = getEffectiveAppName(platform);

        long txTime = System.currentTimeMillis() / 1000 + expireTime;
        String txTimeHex = Long.toHexString(txTime).toUpperCase();
        
        String key = platform.getAppSecret();
        if (key == null || key.isEmpty()) {
            key = tencentLiveProperties.getSecretKey();
        }
        
        String signStr = key + appName + streamId + txTimeHex;
        String txSecret = md5Encode(signStr);

        String pushUrl = String.format("rtmp://%s/%s/%s?txSecret=%s&txTime=%s",
            pushDomain, appName, streamId, txSecret, txTimeHex);
        
        String playUrlFlv = String.format("https://%s/%s/%s.flv?txSecret=%s&txTime=%s",
            playDomain, appName, streamId, txSecret, txTimeHex);
        
        String playUrlHls = String.format("https://%s/%s/%s.m3u8?txSecret=%s&txTime=%s",
            playDomain, appName, streamId, txSecret, txTimeHex);

        return LiveAddress.builder()
                .pushUrl(pushUrl)
                .playUrl(playUrlHls)
                .flvUrl(playUrlFlv)
                .hlsUrl(playUrlHls)
                .streamId(streamId)
                .build();
    }

    /**
     * 获取有效的推流域名
     */
    private String getEffectivePushDomain(LivePlatform platform) {
        if (platform.getPushDomain() != null && !platform.getPushDomain().isEmpty()) {
            return platform.getPushDomain();
        }
        return tencentLiveProperties.getPushDomain();
    }

    /**
     * 获取有效的播放域名
     */
    private String getEffectivePlayDomain(LivePlatform platform) {
        if (platform.getPlayDomain() != null && !platform.getPlayDomain().isEmpty()) {
            return platform.getPlayDomain();
        }
        return tencentLiveProperties.getPlayDomain();
    }

    /**
     * 获取有效的应用名称
     */
    private String getEffectiveAppName(LivePlatform platform) {
        if (platform.getPushUrlTemplate() != null && !platform.getPushUrlTemplate().isEmpty()) {
            return "live";
        }
        return tencentLiveProperties.getAppName();
    }

    /**
     * MD5加密
     */
    private String md5Encode(String str) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(str.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("MD5加密失败", e);
            return "";
        }
    }

    /**
     * 生成阿里云直播地址（可选）
     */
    private LiveAddress generateAliyunAddress(LivePlatform platform, String streamId, Integer expireTime) {
        String pushDomain = platform.getPushDomain() != null ? platform.getPushDomain() : "push.aliyun.livehouse.cn";
        String playDomain = platform.getPlayDomain() != null ? platform.getPlayDomain() : "play.aliyun.livehouse.cn";

        String pushUrl = String.format("rtmp://%s/live/%s", pushDomain, streamId);
        String playUrl = String.format("https://%s/live/%s.m3u8", playDomain, streamId);

        return LiveAddress.builder()
                .pushUrl(pushUrl)
                .playUrl(playUrl)
                .streamId(streamId)
                .build();
    }

    /**
     * 生成抖音直播地址（可选）
     */
    private LiveAddress generateDouyinAddress(LivePlatform platform, String streamId, Integer expireTime) {
        return LiveAddress.builder()
                .pushUrl("rtmp://push.douyin.livehouse.cn/live/" + streamId)
                .playUrl("https://play.douyin.livehouse.cn/live/" + streamId + ".m3u8")
                .streamId(streamId)
                .build();
    }
}