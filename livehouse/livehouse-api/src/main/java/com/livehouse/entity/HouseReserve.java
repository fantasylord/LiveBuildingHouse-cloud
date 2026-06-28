package com.livehouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 看房预约表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("house_reserve")
public class HouseReserve extends BaseEntity {

    private Long appUserId;

    private Long buildingId;

    private Long unitId;

    private Long liveSessionId;

    private String reserveCode;

    private String customerName;

    private String phone;

    private LocalDateTime visitTime;

    private Integer visitType;

    private Integer visitStatus;
}
