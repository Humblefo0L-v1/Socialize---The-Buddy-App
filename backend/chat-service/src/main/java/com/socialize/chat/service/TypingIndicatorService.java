package com.socialize.chat.service;

import com.socialize.chat.model.dto.TypingIndicatorDTO;
import com.socialize.chat.model.entity.TypingIndicator;
import com.socialize.chat.repository.TypingIndicatorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TypingIndicatorService {
    
    private final TypingIndicatorRepository typingIndicatorRepository;
    private final WebSocketSenderService webSocketSenderService;
    
    /**
     * Update typing indicator
     */
    public TypingIndicatorDTO updateTypingIndicator(String groupChatId, Long userId, Boolean isTyping) {
        log.info("Updating typing indicator for user {} in group {}: {}", 
            userId, groupChatId, isTyping);
        
        if (isTyping) {
            // Create or update typing indicator
            TypingIndicator indicator = typingIndicatorRepository
                .findByGroupChatIdAndUserId(groupChatId, userId)
                .orElse(TypingIndicator.builder()
                    .groupChatId(groupChatId)
                    .userId(userId)
                    .build());
            
            indicator.setIsTyping(true);
            indicator.setTimestamp(LocalDateTime.now());
            
            typingIndicatorRepository.save(indicator);
            
            return convertToDTO(indicator);
        } else {
            // Remove typing indicator
            typingIndicatorRepository.deleteByGroupChatIdAndUserId(groupChatId, userId);
            
            return TypingIndicatorDTO.builder()
                .groupChatId(groupChatId)
                .userId(userId)
                .isTyping(false)
                .timestamp(LocalDateTime.now())
                .build();
        }
    }
    
    /**
     * Get all typing users in a group
     */
    public List<TypingIndicatorDTO> getTypingUsers(String groupChatId) {
        List<TypingIndicator> indicators = typingIndicatorRepository
            .findByGroupChatIdAndIsTypingTrue(groupChatId);
        
        return indicators.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    private TypingIndicatorDTO convertToDTO(TypingIndicator indicator) {
        return TypingIndicatorDTO.builder()
            .groupChatId(indicator.getGroupChatId())
            .userId(indicator.getUserId())
            .username(indicator.getUsername())
            .isTyping(indicator.getIsTyping())
            .timestamp(indicator.getTimestamp())
            .build();
    }
}