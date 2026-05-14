package com.smartsync.auth.service;

import com.rabbitmq.client.Channel;
import com.smartsync.api.dto.ApprovalStatusMessage;
import com.smartsync.auth.config.RabbitMQConfig;
import com.smartsync.auth.entity.SysNotification;
import com.smartsync.auth.mapper.SysNotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalMessageConsumer {

    private final SysNotificationMapper notificationMapper;
    private final ConcurrentHashMap<String, Boolean> processedMessages = new ConcurrentHashMap<>();

    @RabbitListener(queues = RabbitMQConfig.APPROVAL_STATUS_QUEUE)
    public void handleApprovalStatusChange(ApprovalStatusMessage message, Channel channel, Message amqpMessage) throws IOException {
        String messageId = amqpMessage.getMessageProperties().getMessageId();
        long deliveryTag = amqpMessage.getMessageProperties().getDeliveryTag();

        try {
            log.info("收到审批状态变更消息: messageId={}, content={}", messageId, message);

            // 幂等性检查
            if (messageId != null && processedMessages.containsKey(messageId)) {
                log.warn("消息已处理，跳过: messageId={}", messageId);
                channel.basicAck(deliveryTag, false);
                return;
            }

            // 状态 1=待审批，发送给审批人
            if (message.getNewStatus() == 1 && message.getApproverId() != null) {
                createNotification(message.getApproverId(), "审批待办通知",
                        "您有一条新的审批待处理：" + message.getTitle(),
                        message.getApprovalType(), message.getApprovalId());
                log.info("已创建审批待办通知给审批人: userId={}", message.getApproverId());
            }

            // 状态 2=已通过或 3=已驳回，通知申请人
            if (message.getNewStatus() == 2 || message.getNewStatus() == 3) {
                String statusText = message.getNewStatus() == 2 ? "已通过" : "已驳回";
                createNotification(message.getApplicantId(), "审批结果通知",
                        "您的申请[" + message.getTitle() + "]" + statusText,
                        message.getApprovalType(), message.getApprovalId());
                log.info("已创建审批结果通知给申请人: userId={}", message.getApplicantId());
            }

            // 标记为已处理
            if (messageId != null) {
                processedMessages.put(messageId, true);
                // 清理过期标记
                if (processedMessages.size() > 10000) {
                    processedMessages.clear();
                }
            }

            channel.basicAck(deliveryTag, false);
            log.info("审批状态变更消息处理完成: messageId={}", messageId);
        } catch (DuplicateKeyException e) {
            log.warn("消息重复消费，已确认: messageId={}, error={}", messageId, e.getMessage());
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("审批状态变更消息处理失败: messageId={}, error={}", messageId, e.getMessage(), e);
            // 消息重试超过3次，进入死信队列
            Integer retryCount = (Integer) amqpMessage.getMessageProperties().getHeaders().get("x-retry-count");
            if (retryCount == null) {
                retryCount = 0;
            }
            if (retryCount >= 3) {
                log.error("消息重试次数超过限制，进入死信队列: messageId={}", messageId);
                channel.basicNack(deliveryTag, false, false);
            } else {
                amqpMessage.getMessageProperties().setHeader("x-retry-count", retryCount + 1);
                channel.basicNack(deliveryTag, false, true);
            }
        }
    }

    private void createNotification(Long userId, String title, String content, String businessType, Long businessId) {
        if (userId == null) {
            log.warn("用户ID为空，跳过通知创建");
            return;
        }
        SysNotification notification = new SysNotification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType("APPROVAL");
        notification.setBusinessType(businessType);
        notification.setBusinessId(businessId);
        notification.setIsRead(0);
        notification.setCreateTime(LocalDateTime.now());
        notificationMapper.insert(notification);
    }
}
