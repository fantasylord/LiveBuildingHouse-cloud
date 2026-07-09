package com.livehouse.service.impl;

import com.livehouse.service.RabbitMqMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "livehouse.rabbitmq", name = "enabled", havingValue = "false", matchIfMissing = true)
public class DisabledRabbitMqMessageServiceImpl implements RabbitMqMessageService {

    @Override
    public boolean send(String routingKey, String businessType, Object payload) {
        log.debug("RabbitMQ disabled, skip message - routingKey: {}, businessType: {}", routingKey, businessType);
        return false;
    }

    @Override
    public boolean send(String exchange, String routingKey, String businessType, Object payload) {
        log.debug("RabbitMQ disabled, skip message - exchange: {}, routingKey: {}, businessType: {}",
                exchange, routingKey, businessType);
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
