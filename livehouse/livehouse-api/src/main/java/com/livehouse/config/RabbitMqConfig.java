package com.livehouse.config;

import com.livehouse.common.constants.RabbitMqConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@EnableRabbit
@Configuration
@ConditionalOnProperty(prefix = "livehouse.rabbitmq", name = "enabled", havingValue = "true")
public class RabbitMqConfig {

    @Bean
    public TopicExchange livehouseExchange() {
        return new TopicExchange(RabbitMqConstants.LIVEHOUSE_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(RabbitMqConstants.DEAD_LETTER_EXCHANGE, true, false);
    }

    @Bean
    public Queue messageNoticeQueue() {
        return QueueBuilder.durable(RabbitMqConstants.MESSAGE_NOTICE_QUEUE)
                .deadLetterExchange(RabbitMqConstants.DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(RabbitMqConstants.DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue liveEventQueue() {
        return QueueBuilder.durable(RabbitMqConstants.LIVE_EVENT_QUEUE)
                .deadLetterExchange(RabbitMqConstants.DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(RabbitMqConstants.DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(RabbitMqConstants.DEAD_LETTER_QUEUE).build();
    }

    @Bean
    public Binding messageNoticeBinding(Queue messageNoticeQueue, TopicExchange livehouseExchange) {
        return BindingBuilder.bind(messageNoticeQueue)
                .to(livehouseExchange)
                .with(RabbitMqConstants.MESSAGE_NOTICE_ROUTING_KEY);
    }

    @Bean
    public Binding liveEventBinding(Queue liveEventQueue, TopicExchange livehouseExchange) {
        return BindingBuilder.bind(liveEventQueue)
                .to(livehouseExchange)
                .with(RabbitMqConstants.LIVE_EVENT_ROUTING_KEY);
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, TopicExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue)
                .to(deadLetterExchange)
                .with(RabbitMqConstants.DEAD_LETTER_ROUTING_KEY);
    }

    @Bean
    public MessageConverter rabbitMessageConverter() {
        log.info("RabbitMQ message converter initialized");
        return new Jackson2JsonMessageConverter();
    }
}
