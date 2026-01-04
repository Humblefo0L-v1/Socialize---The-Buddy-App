package com.socialize.chat.service;

import com.socialize.chat.exception.GroupChatNotFoundException;
import com.socialize.chat.model.dto.CreateGroupChatRequest;
import com.socialize.chat.model.dto.GroupChatDTO;
import com.socialize.chat.model.entity.GroupChat;
import com.socialize.chat.repository.CustomGroupChatRepository;
import com.socialize.chat.repository.GroupChatRepository;
import com.socialize.chat.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupChatService {
    
    private final GroupChatRepository groupChatRepository;
    private final CustomGroupChatRepository customGroupChatRepository;
    private final MessageRepository messageRepository;
    
    /**
     * Create a new group chat
     */
    @Transactional
    public GroupChatDTO createGroupChat(CreateGroupChatRequest request) {
        log.info("Creating group chat for event: {}", request.getEventId());
        
        // Check if group already exists for this event
        if (groupChatRepository.existsByEventId(request.getEventId())) {
            throw new IllegalArgumentException("Group chat already exists for this event");
        }
        
        // Create group chat
        GroupChat groupChat = GroupChat.builder()
            .eventId(request.getEventId())
            .groupName(request.getGroupName())
            .participants(new HashSet<>(request.getParticipants()))
            .admins(new HashSet<>())
            .createdBy(request.getCreatedBy())
            .createdAt(LocalDateTime.now())
            .messageCount(0)
            .isActive(true)
            .build();
        
        // Add creator as admin
        groupChat.getAdmins().add(request.getCreatedBy());
        
        GroupChat savedGroup = groupChatRepository.save(groupChat);
        log.info("Group chat created with ID: {}", savedGroup.getId());
        
        return convertToGroupChatDTO(savedGroup, 0);
    }
    
    /**
     * Get group chat by ID
     */
    @Cacheable(value = "groupChats", key = "#groupChatId")
    public GroupChatDTO getGroupChatById(String groupChatId, Long userId) {
        log.info("Fetching group chat: {}", groupChatId);
        
        GroupChat groupChat = groupChatRepository.findById(groupChatId)
            .orElseThrow(() -> new GroupChatNotFoundException("Group chat not found"));
        
        // Get unread count for user
        Long unreadCount = messageRepository.countUnreadMessages(groupChatId, userId);
        
        return convertToGroupChatDTO(groupChat, unreadCount.intValue());
    }
    
    /**
     * Get group chat by event ID
     */
    public GroupChatDTO getGroupChatByEventId(Long eventId, Long userId) {
        log.info("Fetching group chat for event: {}", eventId);
        
        GroupChat groupChat = groupChatRepository.findByEventId(eventId)
            .orElseThrow(() -> new GroupChatNotFoundException(
                "Group chat not found for event: " + eventId));
        
        Long unreadCount = messageRepository.countUnreadMessages(groupChat.getId(), userId);
        
        return convertToGroupChatDTO(groupChat, unreadCount.intValue());
    }
    
    /**
     * Get all group chats for a user
     */
    public List<GroupChatDTO> getUserGroupChats(Long userId) {
        log.info("Fetching group chats for user: {}", userId);
        
        List<GroupChat> groupChats = groupChatRepository.findByParticipantsContaining(userId);
        
        return groupChats.stream()
            .map(gc -> {
                Long unreadCount = messageRepository.countUnreadMessages(gc.getId(), userId);
                return convertToGroupChatDTO(gc, unreadCount.intValue());
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Add participant to group
     */
    @CacheEvict(value = "groupChats", key = "#groupChatId")
    public void addParticipant(String groupChatId, Long userId) {
        log.info("Adding participant {} to group {}", userId, groupChatId);
        
        boolean added = customGroupChatRepository.addParticipant(groupChatId, userId);
        if (!added) {
            throw new IllegalArgumentException("Failed to add participant");
        }
    }
    
    /**
     * Remove participant from group
     */
    @CacheEvict(value = "groupChats", key = "#groupChatId")
    public void removeParticipant(String groupChatId, Long userId) {
        log.info("Removing participant {} from group {}", userId, groupChatId);
        
        customGroupChatRepository.removeParticipant(groupChatId, userId);
    }
    
    /**
     * Update group settings
     */
    @CacheEvict(value = "groupChats", key = "#groupChatId")
    public void updateGroupSettings(String groupChatId, GroupChat.GroupSettings settings) {
        log.info("Updating settings for group: {}", groupChatId);
        
        customGroupChatRepository.updateGroupSettings(groupChatId, settings);
    }
    
    /**
     * Archive group chat
     */
    @CacheEvict(value = "groupChats", key = "#groupChatId")
    public void archiveGroupChat(String groupChatId) {
        log.info("Archiving group chat: {}", groupChatId);
        
        customGroupChatRepository.archiveGroup(groupChatId);
    }
    
    /**
     * Convert GroupChat entity to DTO
     */
    private GroupChatDTO convertToGroupChatDTO(GroupChat groupChat, int unreadCount) {
        return GroupChatDTO.builder()
            .id(groupChat.getId())
            .eventId(groupChat.getEventId())
            .groupName(groupChat.getGroupName())
            .groupImage(groupChat.getGroupImage())
            .participants(groupChat.getParticipants())
            .admins(groupChat.getAdmins())
            .createdBy(groupChat.getCreatedBy())
            .createdAt(groupChat.getCreatedAt())
            .lastMessageAt(groupChat.getLastMessageAt())
            .lastMessage(groupChat.getLastMessage())
            .lastMessageSenderId(groupChat.getLastMessageSenderId())
            .messageCount(groupChat.getMessageCount())
            .unreadCount(unreadCount)
            .isActive(groupChat.getIsActive())
            .build();
    }
}