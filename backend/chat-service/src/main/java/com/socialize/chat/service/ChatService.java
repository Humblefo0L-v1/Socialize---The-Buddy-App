package com.socialize.chat.service;

import com.socialize.chat.exception.GroupChatNotFoundException;
import com.socialize.chat.exception.MessageNotFoundException;
import com.socialize.chat.kafka.ChatKafkaProducer;
import com.socialize.chat.model.dto.*;
import com.socialize.chat.model.entity.GroupChat;
import com.socialize.chat.model.entity.Message;
import com.socialize.chat.model.entity.MessageType;
import com.socialize.chat.repository.CustomMessageRepository;
import com.socialize.chat.repository.GroupChatRepository;
import com.socialize.chat.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {
    
    private final MessageRepository messageRepository;
    private final GroupChatRepository groupChatRepository;
    private final CustomMessageRepository customMessageRepository;
    private final WebSocketSenderService webSocketSenderService;
    private final ChatKafkaProducer kafkaProducer;
    
    /**
     * Send message via WebSocket
     */
    public MessageDTO sendMessageViaWebSocket(SendMessageRequest request, Long senderId) {
        log.info("Sending message from user {} to group {}", senderId, request.getGroupChatId());
        
        // Verify group exists
        GroupChat groupChat = groupChatRepository.findById(request.getGroupChatId())
            .orElseThrow(() -> new GroupChatNotFoundException("Group chat not found"));
        
        // Verify sender is participant
        if (!groupChat.getParticipants().contains(senderId)) {
            throw new IllegalArgumentException("User is not a participant in this group");
        }
        
        // Create message
        Message message = Message.builder()
            .groupChatId(request.getGroupChatId())
            .senderId(senderId)
            .messageType(request.getMessageType())
            .content(request.getContent())
            .mediaUrl(request.getMediaUrl())
            .replyToMessageId(request.getReplyToMessageId())
            .timestamp(LocalDateTime.now())
            .isDeleted(false)
            .isEdited(false)
            .build();
        
        // Save message
        Message savedMessage = messageRepository.save(message);
        
        // Update group chat last message
        updateGroupLastMessage(groupChat.getId(), savedMessage);
        
        // Send to Kafka for notifications
        kafkaProducer.sendNewMessageEvent(savedMessage);
        
        // Convert to DTO
        return convertToMessageDTO(savedMessage);
    }
    
    /**
     * Send message via REST API (fallback)
     */
    @CacheEvict(value = "messages", key = "#request.groupChatId")
    public MessageDTO sendMessage(SendMessageRequest request, Long senderId, 
                                  String senderName, String senderProfileImage) {
        log.info("Sending message via REST from user {} to group {}", 
            senderId, request.getGroupChatId());
        
        // Verify group exists
        GroupChat groupChat = groupChatRepository.findById(request.getGroupChatId())
            .orElseThrow(() -> new GroupChatNotFoundException("Group chat not found"));
        
        // Create message
        Message message = Message.builder()
            .groupChatId(request.getGroupChatId())
            .senderId(senderId)
            .senderName(senderName)
            .senderProfileImage(senderProfileImage)
            .messageType(request.getMessageType())
            .content(request.getContent())
            .mediaUrl(request.getMediaUrl())
            .replyToMessageId(request.getReplyToMessageId())
            .timestamp(LocalDateTime.now())
            .isDeleted(false)
            .isEdited(false)
            .build();
        
        // Save message
        Message savedMessage = messageRepository.save(message);
        
        // Update group chat
        updateGroupLastMessage(groupChat.getId(), savedMessage);
        
        // Send via WebSocket
        MessageDTO messageDTO = convertToMessageDTO(savedMessage);
        webSocketSenderService.sendMessageToGroup(groupChat.getId(), messageDTO);
        
        // Send to Kafka
        kafkaProducer.sendNewMessageEvent(savedMessage);
        
        return messageDTO;
    }
    
    /**
     * Get message history for a group
     */
    @Cacheable(value = "messages", key = "#groupChatId + '_' + #page")
    public Page<MessageDTO> getMessageHistory(String groupChatId, int page, int size) {
        log.info("Fetching message history for group: {}, page: {}", groupChatId, page);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messages = messageRepository
            .findByGroupChatIdAndIsDeletedFalseOrderByTimestampDesc(groupChatId, pageable);
        
        return messages.map(this::convertToMessageDTO);
    }
    
    /**
     * Get unread messages for a user in a group
     */
    public List<MessageDTO> getUnreadMessages(String groupChatId, Long userId) {
        log.info("Fetching unread messages for user {} in group {}", userId, groupChatId);
        
        List<Message> unreadMessages = messageRepository.findUnreadMessages(groupChatId, userId);
        return unreadMessages.stream()
            .map(this::convertToMessageDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get unread message count
     */
    public Long getUnreadCount(String groupChatId, Long userId) {
        return messageRepository.countUnreadMessages(groupChatId, userId);
    }
    
    /**
     * Mark message as read
     */
    public MessageDTO markMessageAsRead(String messageId, Long userId) {
        log.info("Marking message {} as read by user {}", messageId, userId);
        
        boolean updated = customMessageRepository.markAsRead(messageId, userId);
        
        if (!updated) {
            throw new MessageNotFoundException("Message not found or already read");
        }
        
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new MessageNotFoundException("Message not found"));
        
        // Broadcast read receipt
        MessageDTO messageDTO = convertToMessageDTO(message);
        webSocketSenderService.sendMessageToGroup(message.getGroupChatId(), messageDTO);
        
        return messageDTO;
    }
    
    /**
     * Mark all messages as read in a group
     */
    @CacheEvict(value = "messages", key = "#groupChatId")
    public long markAllAsRead(String groupChatId, Long userId) {
        log.info("Marking all messages as read in group {} for user {}", groupChatId, userId);
        return customMessageRepository.markAllAsRead(groupChatId, userId);
    }
    
    /**
     * Add reaction to message
     */
    public MessageDTO addReaction(String messageId, Long userId, String emoji) {
        log.info("Adding reaction {} to message {} by user {}", emoji, messageId, userId);
        
        boolean added = customMessageRepository.addReaction(messageId, userId, emoji);
        
        if (!added) {
            throw new MessageNotFoundException("Message not found");
        }
        
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new MessageNotFoundException("Message not found"));
        
        MessageDTO messageDTO = convertToMessageDTO(message);
        webSocketSenderService.sendMessageToGroup(message.getGroupChatId(), messageDTO);
        
        return messageDTO;
    }
    
    /**
     * Remove reaction from message
     */
    public MessageDTO removeReaction(String messageId, Long userId, String emoji) {
        log.info("Removing reaction {} from message {} by user {}", emoji, messageId, userId);
        
        customMessageRepository.removeReaction(messageId, userId, emoji);
        
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new MessageNotFoundException("Message not found"));
        
        MessageDTO messageDTO = convertToMessageDTO(message);
        webSocketSenderService.sendMessageToGroup(message.getGroupChatId(), messageDTO);
        
        return messageDTO;
    }
    
    /**
     * Delete message (soft delete)
     */
    @CacheEvict(value = "messages", key = "#messageId")
    public void deleteMessage(String messageId) {
        log.info("Deleting message: {}", messageId);
        
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new MessageNotFoundException("Message not found"));
        
        customMessageRepository.softDeleteMessage(messageId);
        
        // Notify via WebSocket
        MessageDTO messageDTO = convertToMessageDTO(message);
        messageDTO.setIsDeleted(true);
        webSocketSenderService.sendMessageToGroup(message.getGroupChatId(), messageDTO);
    }
    
    /**
     * Edit message
     */
    @CacheEvict(value = "messages", key = "#messageId")
    public MessageDTO editMessage(String messageId, String newContent) {
        log.info("Editing message: {}", messageId);
        
        customMessageRepository.editMessage(messageId, newContent);
        
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new MessageNotFoundException("Message not found"));
        
        MessageDTO messageDTO = convertToMessageDTO(message);
        webSocketSenderService.sendMessageToGroup(message.getGroupChatId(), messageDTO);
        
        return messageDTO;
    }
    
    /**
     * Search messages
     */
    public List<MessageDTO> searchMessages(String groupChatId, String searchTerm) {
        log.info("Searching messages in group {} with term: {}", groupChatId, searchTerm);
        
        List<Message> messages = customMessageRepository.searchMessages(groupChatId, searchTerm);
        return messages.stream()
            .map(this::convertToMessageDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get media messages in a group
     */
    public List<MessageDTO> getMediaMessages(String groupChatId) {
        log.info("Fetching media messages for group: {}", groupChatId);
        
        List<Message> mediaMessages = messageRepository.findMediaMessages(groupChatId);
        return mediaMessages.stream()
            .map(this::convertToMessageDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Create system message
     */
    public MessageDTO createSystemMessage(String groupChatId, String content) {
        Message message = Message.builder()
            .groupChatId(groupChatId)
            .messageType(MessageType.SYSTEM)
            .content(content)
            .timestamp(LocalDateTime.now())
            .isDeleted(false)
            .build();
        
        Message savedMessage = messageRepository.save(message);
        return convertToMessageDTO(savedMessage);
    }
    
    /**
     * Update group chat's last message info
     */
    private void updateGroupLastMessage(String groupChatId, Message message) {
        String lastMessagePreview = message.getMessageType() == MessageType.TEXT 
            ? message.getContent() 
            : message.getMessageType().toString();
        
        customMessageRepository.updateLastMessage(
            groupChatId,
            lastMessagePreview,
            message.getSenderId(),
            message.getTimestamp()
        );
    }
    
    /**
     * Convert Message entity to DTO
     */
    private MessageDTO convertToMessageDTO(Message message) {
        return MessageDTO.builder()
            .id(message.getId())
            .groupChatId(message.getGroupChatId())
            .senderId(message.getSenderId())
            .senderName(message.getSenderName())
            .senderProfileImage(message.getSenderProfileImage())
            .messageType(message.getMessageType())
            .content(message.getContent())
            .mediaUrl(message.getMediaUrl())
            .thumbnailUrl(message.getThumbnailUrl())
            .fileSize(message.getFileSize())
            .fileName(message.getFileName())
            .mimeType(message.getMimeType())
            .replyToMessageId(message.getReplyToMessageId())
            .reactions(message.getReactions() != null ? 
                message.getReactions().stream()
                    .map(r -> ReactionDTO.builder()
                        .userId(r.getUserId())
                        .emoji(r.getEmoji())
                        .timestamp(r.getTimestamp())
                        .build())
                    .collect(Collectors.toList()) : null)
            .readBy(message.getReadBy())
            .deliveredTo(message.getDeliveredTo())
            .timestamp(message.getTimestamp())
            .isDeleted(message.getIsDeleted())
            .isEdited(message.getIsEdited())
            .build();
    }
}
