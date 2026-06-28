package com.livehouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_message_template")
public class SysMessageTemplate extends BaseEntity {

    private String templateCode;

    private String templateName;

    private Integer msgType;

    private String titleTemplate;

    private String contentTemplate;

    private String smsTemplate;

    private String variables;

    private Integer status;
}
