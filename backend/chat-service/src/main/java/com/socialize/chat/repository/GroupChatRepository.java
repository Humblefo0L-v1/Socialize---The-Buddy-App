package com.socialize.chat.repository;

import com.socialize.chat.model.entity.GroupChat;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupChatRepository extends MongoRepository<GroupChat, String> {
    
    // Find by event ID
    Optional<GroupChat> findByEventId(Long eventId);
    
    // Check if group exists for event
    boolean existsByEventId(Long eventId);
    
    // Find all groups where user is a participant
    @Query("{ 'participants': ?0, 'isActive': true }")
    List<GroupChat> findByParticipantsContaining(Long userId);
    
    // Find all groups where user is admin
    @Query("{ 'admins': ?0, 'isActive': true }")
    List<GroupChat> findByAdminsContaining(Long userId);
    
    // Find groups created by user
    List<GroupChat> findByCreatedByAndIsActiveTrue(Long userId);
    
    // Find active groups
    List<GroupChat> findByIsActiveTrueOrderByLastMessageAtDesc();
    
    // Find groups by name (search)
    @Query("{ 'groupName': { $regex: ?0, $options: 'i' }, 'isActive': true }")
    List<GroupChat> searchByGroupName(String keyword);
}