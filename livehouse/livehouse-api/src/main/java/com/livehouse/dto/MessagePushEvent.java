package com.livehouse.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MessagePushEvent {

    private Long messageId;

    private Integer receiverType;

    private List<Long> receiverIds;

    private Integer receiverCount;

    private LocalDateTime createTime;
}
