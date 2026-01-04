package com.socialize.chat.repository;

import com.socialize.chat.model.entity.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CustomMessageRepository {
    
    private final MongoTemplate mongoTemplate;
    
    /**
     * Mark message as read by user
     */
    public boolean markAsRead(String messageId, Long userId) {
        Query query = new Query(Criteria.where("_id").is(messageId));
        Update update = new Update().addToSet("readBy", userId);
        
        var result = mongoTemplate.updateFirst(query, update, Message.class);
        return result.getModifiedCount() > 0;
    }

    /**
     * Update group's last message preview and metadata
     */
    public void updateLastMessage(String groupChatId, String lastMessagePreview, Long senderId, LocalDateTime timestamp) {
        Query query = new Query(Criteria.where("_id").is(groupChatId));
        Update update = new Update()
            .set("lastMessage", lastMessagePreview)
            .set("lastMessageSenderId", senderId)
            .set("lastMessageAt", timestamp);

        mongoTemplate.updateFirst(query, update, com.socialize.chat.model.entity.GroupChat.class);
    }
    
    /**
     * Mark message as delivered to user
     */
    public boolean markAsDelivered(String messageId, Long userId) {
        Query query = new Query(Criteria.where("_id").is(messageId));
        Update update = new Update().addToSet("deliveredTo", userId);
        
        var result = mongoTemplate.updateFirst(query, update, Message.class);
        return result.getModifiedCount() > 0;
    }
    
    /**
     * Mark all messages in group as read by user
     */
    public long markAllAsRead(String groupChatId, Long userId) {
        Query query = new Query(
            Criteria.where("groupChatId").is(groupChatId)
                .and("senderId").ne(userId)
                .and("readBy").ne(userId)
                .and("isDeleted").is(false)
        );
        Update update = new Update().addToSet("readBy", userId);
        
        var result = mongoTemplate.updateMulti(query, update, Message.class);
        return result.getModifiedCount();
    }
    
    /**
     * Add reaction to message
     */
    public boolean addReaction(String messageId, Long userId, String emoji) {
        Query query = new Query(Criteria.where("_id").is(messageId));
        Update update = new Update().push("reactions")
            .value(Message.Reaction.builder()
                .userId(userId)
                .emoji(emoji)
                .timestamp(LocalDateTime.now())
                .build());
        
        var result = mongoTemplate.updateFirst(query, update, Message.class);
        return result.getModifiedCount() > 0;
    }
    
    /**
     * Remove reaction from message
     */
    public boolean removeReaction(String messageId, Long userId, String emoji) {
        Query query = new Query(Criteria.where("_id").is(messageId));
        Update update = new Update().pull("reactions", 
            Query.query(Criteria.where("userId").is(userId).and("emoji").is(emoji)));
        
        var result = mongoTemplate.updateFirst(query, update, Message.class);
        return result.getModifiedCount() > 0;
    }
    
    /**
     * Soft delete message
     */
    public boolean softDeleteMessage(String messageId) {
        Query query = new Query(Criteria.where("_id").is(messageId));
        Update update = new Update()
            .set("isDeleted", true)
            .set("deletedAt", LocalDateTime.now());
        
        var result = mongoTemplate.updateFirst(query, update, Message.class);
        return result.getModifiedCount() > 0;
    }
    
    /**
     * Edit message content
     */
    public boolean editMessage(String messageId, String newContent) {
        Query query = new Query(Criteria.where("_id").is(messageId));
        Update update = new Update()
            .set("content", newContent)
            .set("isEdited", true)
            .set("editedAt", LocalDateTime.now());
        
        var result = mongoTemplate.updateFirst(query, update, Message.class);
        return result.getModifiedCount() > 0;
    }
    
    /**
     * Search messages by content
     */
    public List<Message> searchMessages(String groupChatId, String searchTerm) {
        Query query = new Query(
            Criteria.where("groupChatId").is(groupChatId)
                .and("content").regex(searchTerm, "i")
                .and("isDeleted").is(false)
        );
        query.limit(50);  // Limit search results
        
        return mongoTemplate.find(query, Message.class);
    }
}