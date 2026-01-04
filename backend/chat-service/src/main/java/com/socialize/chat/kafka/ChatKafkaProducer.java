package com.socialize.chat.kafka;

import com.socialize.chat.model.entity.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatKafkaProducer {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Value("${kafka.topics.new-message}")
    private String newMessageTopic;
    
    @Value("${kafka.topics.message-read}")
    private String messageReadTopic;
    
    @Value("${kafka.topics.typing-indicator}")
    private String typingIndicatorTopic;
    
    /**
     * Send new message event to Kafka
     */
    public void sendNewMessageEvent(Message message) {
        Map<String, Object> event = new HashMap<>();
        event.put("messageId", message.getId());
        event.put("groupChatId", message.getGroupChatId());
        event.put("senderId", message.getSenderId());
        event.put("senderName", message.getSenderName());
        event.put("messageType", message.getMessageType());
        event.put("content", message.getContent());
        event.put("timestamp", message.getTimestamp());
        
        CompletableFuture<SendResult<String, Object>> future = 
            kafkaTemplate.send(newMessageTopic, message.getGroupChatId(), event);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Sent new message event to Kafka: messageId={}", message.getId());
            } else {
                log.error("Failed to send new message event to Kafka: {}", ex.getMessage());
            }
        });
    }
    
    /**
     * Send message read event to Kafka
     */
    public void sendMessageReadEvent(String messageId, Long userId, String groupChatId) {
        Map<String, Object> event = new HashMap<>();
        event.put("messageId", messageId);
        event.put("userId", userId);
        event.put("groupChatId", groupChatId);
        event.put("timestamp", System.currentTimeMillis());
        
        kafkaTemplate.send(messageReadTopic, messageId, event)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Sent message read event to Kafka: messageId={}", messageId);
                } else {
                    log.error("Failed to send message read event: {}", ex.getMessage());
                }
            });
    }
    
    /**
     * Send typing indicator event to Kafka
     */
    public void sendTypingIndicatorEvent(String groupChatId, Long userId, Boolean isTyping) {
        Map<String, Object> event = new HashMap<>();
        event.put("groupChatId", groupChatId);
        event.put("userId", userId);
        event.put("isTyping", isTyping);
        event.put("timestamp", System.currentTimeMillis());
        
        kafkaTemplate.send(typingIndicatorTopic, groupChatId, event)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.debug("Sent typing indicator event to Kafka");
                } else {
                    log.error("Failed to send typing indicator event: {}", ex.getMessage());
                }
            });
    }
}