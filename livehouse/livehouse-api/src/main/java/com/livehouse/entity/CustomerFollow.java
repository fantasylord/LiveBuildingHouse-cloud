package com.livehouse.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 客户跟进记录表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("customer_follow")
public class CustomerFollow extends BaseEntity {

    private Long customerId;

    private Long consultantId;

    private Integer followType;

    private String followContent;

    private Integer intentionLevel;

    private String nextPlan;

    private LocalDateTime nextFollowTime;

    @TableField(exist = false)
    private String customerName;

    @TableField(exist = false)
    private String customerPhone;
}
