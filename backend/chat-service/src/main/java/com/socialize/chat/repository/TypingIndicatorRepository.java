package com.socialize.chat.repository;

import com.socialize.chat.model.entity.TypingIndicator;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TypingIndicatorRepository extends MongoRepository<TypingIndicator, String> {
    
    // Find typing indicators for a group chat
    List<TypingIndicator> findByGroupChatIdAndIsTypingTrue(String groupChatId);
    
    // Find specific user's typing indicator in a group
    Optional<TypingIndicator> findByGroupChatIdAndUserId(String groupChatId, Long userId);
    
    // Delete typing indicator for user in group
    void deleteByGroupChatIdAndUserId(String groupChatId, Long userId);
    
    // Delete all typing indicators for a group
    void deleteByGroupChatId(String groupChatId);
}