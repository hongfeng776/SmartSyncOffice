package com.smartsync.auth.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RabbitMQConfig {

    public static final String APPROVAL_EXCHANGE = "approval.exchange";
    public static final String APPROVAL_STATUS_QUEUE = "approval.status.queue";
    public static final String APPROVAL_STATUS_DLQ = "approval.status.dlq";
    public static final String APPROVAL_STATUS_ROUTING_KEY = "approval.status";
    public static final String DLX_EXCHANGE = "dlx.exchange";

    @Bean
    public DirectExchange approvalExchange() {
        return ExchangeBuilder.directExchange(APPROVAL_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public DirectExchange dlxExchange() {
        return ExchangeBuilder.directExchange(DLX_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Queue approvalStatusQueue() {
        return QueueBuilder.durable(APPROVAL_STATUS_QUEUE)
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey(APPROVAL_STATUS_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue approvalStatusDlq() {
        return QueueBuilder.durable(APPROVAL_STATUS_DLQ).build();
    }

    @Bean
    public Binding approvalStatusBinding(Queue approvalStatusQueue, DirectExchange approvalExchange) {
        return BindingBuilder.bind(approvalStatusQueue).to(approvalExchange).with(APPROVAL_STATUS_ROUTING_KEY);
    }

    @Bean
    public Binding approvalStatusDlqBinding(Queue approvalStatusDlq, DirectExchange dlxExchange) {
        return BindingBuilder.bind(approvalStatusDlq).to(dlxExchange).with(APPROVAL_STATUS_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        rabbitTemplate.setConfirmCallback((CorrelationData correlationData, boolean ack, String cause) -> {
            if (ack) {
                log.info("消息发送成功: {}", correlationData != null ? correlationData.getId() : "unknown");
            } else {
                log.error("消息发送失败: {}, cause: {}", correlationData != null ? correlationData.getId() : "unknown", cause);
            }
        });
        rabbitTemplate.setReturnsCallback((ReturnedMessage returned) -> {
            log.error("消息被退回: exchange={}, routingKey={}, message={}",
                    returned.getExchange(), returned.getRoutingKey(), new String(returned.getMessage().getBody()));
        });
        return rabbitTemplate;
    }
}
