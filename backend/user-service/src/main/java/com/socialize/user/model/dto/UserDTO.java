package com.socialize.user.model.dto;

import com.socialize.common.dto.LocationDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String bio;
    private String profilePictureUrl;
    private Boolean isActive;
    private Boolean emailVerified;
    private LocationDTO location;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}