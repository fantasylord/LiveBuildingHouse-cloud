package com.livehouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 直播白名单表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("live_whitelist")
public class LiveWhitelist extends BaseEntity {

    private Long sessionId;

    private String phone;

    private String customerName;
}