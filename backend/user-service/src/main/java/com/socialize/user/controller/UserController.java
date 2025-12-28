// UserController.java
package com.socialize.user.controller;

import com.socialize.common.dto.ApiResponse;
import com.socialize.common.dto.LocationDTO;
import com.socialize.user.model.dto.UpdateLocationRequest;
import com.socialize.user.model.dto.UpdateProfileRequest;
import com.socialize.user.model.dto.UserDTO;
import com.socialize.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        UserDTO user = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long userId) {
        UserDTO user = userService.getUserProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserDTO currentUser = userService.getUserByEmail(userDetails.getUsername());
        UserDTO updatedUser = userService.updateProfile(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updatedUser));
    }

    @PostMapping("/me/location")
    public ResponseEntity<ApiResponse<LocationDTO>> updateLocation(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateLocationRequest request) {
        UserDTO currentUser = userService.getUserByEmail(userDetails.getUsername());
        LocationDTO location = userService.updateLocation(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Location updated successfully", location));
    }

    @GetMapping("/me/location")
    public ResponseEntity<ApiResponse<LocationDTO>> getMyLocation(@AuthenticationPrincipal UserDetails userDetails) {
        UserDTO currentUser = userService.getUserByEmail(userDetails.getUsername());
        LocationDTO location = userService.getUserLocation(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(location));
    }

    @GetMapping("/{userId}/location")
    public ResponseEntity<ApiResponse<LocationDTO>> getUserLocation(@PathVariable Long userId) {
        LocationDTO location = userService.getUserLocation(userId);
        return ResponseEntity.ok(ApiResponse.success(location));
    }
}