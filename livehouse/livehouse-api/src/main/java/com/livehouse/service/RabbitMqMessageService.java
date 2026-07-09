package com.livehouse.service;

public interface RabbitMqMessageService {

    boolean send(String routingKey, String businessType, Object payload);

    boolean send(String exchange, String routingKey, String businessType, Object payload);

    boolean isEnabled();
}
