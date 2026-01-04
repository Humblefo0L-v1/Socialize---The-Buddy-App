package com.socialize.chat.repository;

import com.socialize.chat.model.entity.GroupChat;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class CustomGroupChatRepository {
    
    private final MongoTemplate mongoTemplate;
    
    /**
     * Add participant to group
     */
    public boolean addParticipant(String groupChatId, Long userId) {
        Query query = new Query(Criteria.where("_id").is(groupChatId));
        Update update = new Update().addToSet("participants", userId);
        
        var result = mongoTemplate.updateFirst(query, update, GroupChat.class);
        return result.getModifiedCount() > 0;
    }
    
    /**
     * Remove participant from group
     */
    public boolean removeParticipant(String groupChatId, Long userId) {
        Query query = new Query(Criteria.where("_id").is(groupChatId));
        Update update = new Update().pull("participants", userId);
        
        var result = mongoTemplate.updateFirst(query, update, GroupChat.class);
        return result.getModifiedCount() > 0;
    }
    
    /**
     * Add admin to group
     */
    public boolean addAdmin(String groupChatId, Long userId) {
        Query query = new Query(Criteria.where("_id").is(groupChatId));
        Update update = new Update().addToSet("admins", userId);
        
        var result = mongoTemplate.updateFirst(query, update, GroupChat.class);
        return result.getModifiedCount() > 0;
    }
    
    /**
     * Remove admin from group
     */
    public boolean removeAdmin(String groupChatId, Long userId) {
        Query query = new Query(Criteria.where("_id").is(groupChatId));
        Update update = new Update().pull("admins", userId);
        
        var result = mongoTemplate.updateFirst(query, update, GroupChat.class);
        return result.getModifiedCount() > 0;
    }
    
    /**
     * Update last message info
     */
    public boolean updateLastMessage(String groupChatId, String lastMessage, 
                                     Long senderId, LocalDateTime timestamp) {
        Query query = new Query(Criteria.where("_id").is(groupChatId));
        Update update = new Update()
            .set("lastMessage", lastMessage)
            .set("lastMessageSenderId", senderId)
            .set("lastMessageAt", timestamp)
            .inc("messageCount", 1);
        
        var result = mongoTemplate.updateFirst(query, update, GroupChat.class);
        return result.getModifiedCount() > 0;
    }
    
    /**
     * Archive group chat
     */
    public boolean archiveGroup(String groupChatId) {
        Query query = new Query(Criteria.where("_id").is(groupChatId));
        Update update = new Update()
            .set("isActive", false)
            .set("archivedAt", LocalDateTime.now());
        
        var result = mongoTemplate.updateFirst(query, update, GroupChat.class);
        return result.getModifiedCount() > 0;
    }
    
    /**
     * Update group settings
     */
    public boolean updateGroupSettings(String groupChatId, GroupChat.GroupSettings settings) {
        Query query = new Query(Criteria.where("_id").is(groupChatId));
        Update update = new Update().set("settings", settings);
        
        var result = mongoTemplate.updateFirst(query, update, GroupChat.class);
        return result.getModifiedCount() > 0;
    }
}