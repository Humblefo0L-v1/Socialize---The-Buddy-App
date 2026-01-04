package com.socialize.chat.repository;

import com.socialize.chat.model.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {
    
    // Find messages by group chat ID with pagination
    Page<Message> findByGroupChatIdAndIsDeletedFalseOrderByTimestampDesc(
        String groupChatId, Pageable pageable);
    
    // Find messages after a specific timestamp
    List<Message> findByGroupChatIdAndTimestampAfterAndIsDeletedFalse(
        String groupChatId, LocalDateTime timestamp);
    
    // Find unread messages for a user in a group
    @Query("{ 'groupChatId': ?0, 'senderId': { $ne: ?1 }, 'readBy': { $ne: ?1 }, 'isDeleted': false }")
    List<Message> findUnreadMessages(String groupChatId, Long userId);
    
    // Count unread messages for a user in a group
    @Query(value = "{ 'groupChatId': ?0, 'senderId': { $ne: ?1 }, 'readBy': { $ne: ?1 }, 'isDeleted': false }", count = true)
    Long countUnreadMessages(String groupChatId, Long userId);
    
    // Find messages by sender
    List<Message> findBySenderIdAndIsDeletedFalseOrderByTimestampDesc(Long senderId);
    
    // Find messages with media
    @Query("{ 'groupChatId': ?0, 'messageType': { $in: ['IMAGE', 'VIDEO', 'FILE'] }, 'isDeleted': false }")
    List<Message> findMediaMessages(String groupChatId);
    
    // Delete all messages in a group chat
    void deleteByGroupChatId(String groupChatId);
    
    // Count total messages in a group
    long countByGroupChatIdAndIsDeletedFalse(String groupChatId);
    
    // Find latest message in a group
    Message findFirstByGroupChatIdAndIsDeletedFalseOrderByTimestampDesc(String groupChatId);
}