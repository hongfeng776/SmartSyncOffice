package com.smartsync.auth.service;

import com.smartsync.api.dto.ApprovalStatusMessage;
import com.smartsync.auth.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    @Retryable(
            retryFor = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void sendApprovalStatusChangeMessage(ApprovalStatusMessage message) {
        String messageId = UUID.randomUUID().toString();
        try {
            log.info("发送审批状态变更消息: messageId={}, content={}", messageId, message);
            CorrelationData correlationData = new CorrelationData(messageId);
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.APPROVAL_EXCHANGE,
                    RabbitMQConfig.APPROVAL_STATUS_ROUTING_KEY,
                    message,
                    correlationData
            );
            log.info("审批状态变更消息已投递: messageId={}", messageId);
        } catch (Exception e) {
            log.error("审批状态变更消息发送失败, messageId={}, error={}", messageId, e.getMessage(), e);
            throw e;
        }
    }
}
