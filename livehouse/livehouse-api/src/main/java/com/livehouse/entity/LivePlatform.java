package com.livehouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 三方直播平台配置表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("live_platform")
public class LivePlatform extends BaseEntity {

    private String platformName;

    private String platformCode;

    private String platformType;

    private String appId;

    private String appKey;

    private String appSecret;

    private String pushDomain;

    private String playDomain;

    private String pushUrlTemplate;

    private String playUrlTemplate;

    private Integer expireTime;

    private Integer status;

    private String description;
}