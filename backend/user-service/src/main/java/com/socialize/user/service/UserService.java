package com.socialize.user.service;

import com.socialize.common.dto.LocationDTO;
import com.socialize.common.exception.ResourceNotFoundException;
import com.socialize.user.model.dto.UpdateLocationRequest;
import com.socialize.user.model.dto.UpdateProfileRequest;
import com.socialize.user.model.dto.UserDTO;
import com.socialize.user.model.entity.User;
import com.socialize.user.model.entity.UserLocation;
import com.socialize.user.repository.UserLocationRepository;
import com.socialize.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserLocationRepository locationRepository;

    public UserService(UserRepository userRepository, UserLocationRepository locationRepository) {
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
    }

    public UserDTO getUserProfile(Long userId) {
        User user = userRepository.findByIdWithLocation(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToDTO(user);
    }

    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmailWithLocation(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToDTO(user);
    }

    @Transactional
    public UserDTO updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getProfilePictureUrl() != null) {
            user.setProfilePictureUrl(request.getProfilePictureUrl());
        }

        user = userRepository.save(user);
        return mapToDTO(user);
    }

    @Transactional
    public LocationDTO updateLocation(Long userId, UpdateLocationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserLocation location = locationRepository.findByUserId(userId)
                .orElse(UserLocation.builder().user(user).build());

        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());
        location.setAddress(request.getAddress());

        location = locationRepository.save(location);

        return new LocationDTO(
                location.getLatitude(),
                location.getLongitude(),
                location.getAddress()
        );
    }

    public LocationDTO getUserLocation(Long userId) {
        UserLocation location = locationRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found for user"));

        return new LocationDTO(
                location.getLatitude(),
                location.getLongitude(),
                location.getAddress()
        );
    }

    private UserDTO mapToDTO(User user) {
        LocationDTO locationDTO = null;
        if (user.getLocation() != null) {
            locationDTO = new LocationDTO(
                    user.getLocation().getLatitude(),
                    user.getLocation().getLongitude(),
                    user.getLocation().getAddress()
            );
        }

        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .bio(user.getBio())
                .profilePictureUrl(user.getProfilePictureUrl())
                .isActive(user.getIsActive())
                .emailVerified(user.getEmailVerified())
                .location(locationDTO)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}