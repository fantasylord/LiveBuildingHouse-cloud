package com.livehouse.listener;

import com.livehouse.common.constants.RabbitMqConstants;
import com.livehouse.dto.RabbitMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "livehouse.rabbitmq", name = "enabled", havingValue = "true")
public class MessageNoticeRabbitListener {

    @RabbitListener(queues = RabbitMqConstants.MESSAGE_NOTICE_QUEUE)
    public void handleMessageNotice(RabbitMessage<?> message) {
        log.info("RabbitMQ message notice received - messageId: {}, businessType: {}",
                message.getMessageId(), message.getBusinessType());
    }

    @RabbitListener(queues = RabbitMqConstants.LIVE_EVENT_QUEUE)
    public void handleLiveEvent(RabbitMessage<?> message) {
        log.info("RabbitMQ live event received - messageId: {}, businessType: {}",
                message.getMessageId(), message.getBusinessType());
    }

    @RabbitListener(queues = RabbitMqConstants.DEAD_LETTER_QUEUE)
    public void handleDeadLetter(RabbitMessage<?> message) {
        log.warn("RabbitMQ dead letter received - messageId: {}, businessType: {}",
                message.getMessageId(), message.getBusinessType());
    }
}
