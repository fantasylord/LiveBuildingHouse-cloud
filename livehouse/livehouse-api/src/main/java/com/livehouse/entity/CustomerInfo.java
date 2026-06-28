package com.livehouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 客户信息表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("customer_info")
public class CustomerInfo extends BaseEntity {

    private String customerName;

    private String customerCode;

    private String phone;

    private Integer gender;

    private Integer age;

    private String wechat;

    private String email;

    private String province;

    private String city;

    private String district;

    private String address;

    private BigDecimal budget;

    private String interestedUnit;

    private String customerSource;

    private Integer customerLevel;

    private Integer intentionStatus;

    private Long assignConsultantId;

    private LocalDateTime assignTime;

    private LocalDateTime firstVisitTime;

    private LocalDateTime lastFollowTime;

    private Integer followCount;
}