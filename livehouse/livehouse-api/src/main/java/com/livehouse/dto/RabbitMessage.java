package com.livehouse.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class RabbitMessage<T> {

    private String messageId;

    private String businessType;

    private T payload;

    private LocalDateTime createTime;

    public static <T> RabbitMessage<T> of(String businessType, T payload) {
        RabbitMessage<T> message = new RabbitMessage<>();
        message.setMessageId(UUID.randomUUID().toString());
        message.setBusinessType(businessType);
        message.setPayload(payload);
        message.setCreateTime(LocalDateTime.now());
        return message;
    }
}
