package com.livehouse.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer msgType;

    private String msgCode;

    private String title;

    private String content;

    private Long senderId;

    private String senderName;

    private String linkUrl;

    private String linkParams;

    private Integer sendStatus;

    private LocalDateTime sendTime;

    private String channel;

    private String extData;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private Long createBy;

    private Long updateBy;

    @TableLogic
    private Integer deleted;

    private String remark;

    @TableField(exist = false)
    private Integer receiverCount;

    @TableField(exist = false)
    private Integer readStatus;

    @TableField(exist = false)
    private LocalDateTime readTime;

    @TableField(exist = false)
    private Integer receiverType;
}
