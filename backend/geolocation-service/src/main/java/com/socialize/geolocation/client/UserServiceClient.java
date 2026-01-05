package com.socialize.geolocation.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", path = "/api/users")
public interface UserServiceClient {
    
    @GetMapping("/{userId}")
    UserDTO getUserById(@PathVariable("userId") Long userId);
    
    // DTO for User Service response
    record UserDTO(
        Long id,
        String username,
        String email,
        String profileImageUrl,
        Double averageRating
    ) {}
}