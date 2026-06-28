package com.livehouse.service.impl;

import com.livehouse.config.TencentLiveProperties;
import com.livehouse.entity.LivePlatform;
import com.livehouse.entity.LiveSession;
import com.livehouse.mapper.LivePlatformMapper;
import com.livehouse.service.TencentLiveService;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.live.v20180801.LiveClient;
import com.tencentcloudapi.live.v20180801.models.DescribeLiveStreamStateRequest;
import com.tencentcloudapi.live.v20180801.models.DescribeLiveStreamStateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class TencentLiveServiceImpl implements TencentLiveService {

    private final TencentLiveProperties properties;

    private final LivePlatformMapper livePlatformMapper;

    @Override
    public Integer queryStreamStatus(LiveSession session) {
        if (session == null || !StringUtils.hasText(session.getStreamId()) || session.getPlatformId() == null) {
            return null;
        }
        
        LivePlatform platform = livePlatformMapper.selectById(session.getPlatformId());
        if (platform == null || !"tencent".equalsIgnoreCase(platform.getPlatformType())) {
            return null;
        }
        
        if (!properties.isValid()) {
            log.warn("腾讯云密钥未配置，跳过直播状态主动同步");
            return null;
        }

        try {
            Credential credential = new Credential(properties.getSecretId(), properties.getSecretKey());
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("live.tencentcloudapi.com");
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            LiveClient client = new LiveClient(credential, properties.getLiveRegion(), clientProfile);

            DescribeLiveStreamStateRequest request = new DescribeLiveStreamStateRequest();
            request.setAppName(properties.getAppName());
            request.setDomainName(platform.getPlayDomain() != null ? platform.getPlayDomain() : properties.getPlayDomain());
            request.setStreamName(session.getStreamId());
            DescribeLiveStreamStateResponse response = client.DescribeLiveStreamState(request);
            String state = response.getStreamState();
            
            if ("active".equalsIgnoreCase(state)) {
                return 1;
            }
            if ("inactive".equalsIgnoreCase(state) || "forbid".equalsIgnoreCase(state)) {
                return session.getStatus() != null && session.getStatus() == 1 ? 2 : session.getStatus();
            }
        } catch (Exception e) {
            log.error("查询腾讯云直播状态失败，streamId={}", session.getStreamId(), e);
        }
        return null;
    }
}