package com.livehouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("live_interaction")
public class LiveInteraction extends BaseEntity {

    private Long sessionId;

    private Long appUserId;

    private String clientId;

    /**
     * 1-like, 2-danmu.
     */
    private Integer interactionType;

    private String content;

    private String nickname;

    /**
     * 0-inactive/hidden, 1-active.
     */
    private Integer status;
}
