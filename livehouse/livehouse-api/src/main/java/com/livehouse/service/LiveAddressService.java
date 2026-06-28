package com.livehouse.service;

import com.livehouse.entity.LiveAddress;
import com.livehouse.entity.LivePlatform;

/**
 * 统一直播地址生成服务
 */
public interface LiveAddressService {

    /**
     * 根据平台类型生成直播地址
     */
    LiveAddress generateLiveAddress(LivePlatform platform, String streamId, Integer expireTime);

    /**
     * 生成唯一的StreamId
     */
    String generateStreamId();
}