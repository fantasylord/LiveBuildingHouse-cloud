package com.livehouse.config;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.live.v20180801.LiveClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 腾讯云直播配置类
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "tencent.cloud.enabled", havingValue = "true")
public class TencentLiveConfig {

    private final TencentLiveProperties properties;

    /**
     * 腾讯云凭证Bean
     */
    @Bean
    public Credential tencentCredential() {
        return new Credential(properties.getSecretId(), properties.getSecretKey());
    }

    /**
     * 腾讯云直播API客户端Bean
     */
    @Bean
    public LiveClient tencentLiveClient(Credential credential) {
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint("live.tencentcloudapi.com");

        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);

        LiveClient client = new LiveClient(credential, properties.getLiveRegion(), clientProfile);
        log.info("腾讯云直播客户端初始化完成，区域: {}", properties.getLiveRegion());
        return client;
    }
}