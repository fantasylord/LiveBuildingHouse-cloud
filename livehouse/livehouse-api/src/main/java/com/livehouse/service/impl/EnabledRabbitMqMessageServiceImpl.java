package com.livehouse.service.impl;

import com.livehouse.common.constants.RabbitMqConstants;
import com.livehouse.dto.RabbitMessage;
import com.livehouse.service.RabbitMqMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "livehouse.rabbitmq", name = "enabled", havingValue = "true")
public class EnabledRabbitMqMessageServiceImpl implements RabbitMqMessageService {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public boolean send(String routingKey, String businessType, Object payload) {
        return send(RabbitMqConstants.LIVEHOUSE_EXCHANGE, routingKey, businessType, payload);
    }

    @Override
    public boolean send(String exchange, String routingKey, String businessType, Object payload) {
        RabbitMessage<Object> message = RabbitMessage.of(businessType, payload);
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, message);
            log.info("RabbitMQ message sent - messageId: {}, exchange: {}, routingKey: {}, businessType: {}",
                    message.getMessageId(), exchange, routingKey, businessType);
            return true;
        } catch (AmqpException e) {
            log.error("RabbitMQ message send failed - exchange: {}, routingKey: {}, businessType: {}, error: {}",
                    exchange, routingKey, businessType, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
