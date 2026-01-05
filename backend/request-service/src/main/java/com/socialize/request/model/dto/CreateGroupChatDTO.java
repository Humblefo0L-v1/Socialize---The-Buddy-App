package com.socialize.request.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGroupChatDTO {
    private Long eventId;
    private String groupName;
    private Set<Long> participants;
    private Long createdBy;
}