package com.socialize.rating.controller;

import com.socialize.common.dto.ApiResponse;
import com.socialize.rating.model.dto.RatingDTO;
import com.socialize.rating.model.dto.SubmitRatingRequest;
import com.socialize.rating.model.dto.TrustScoreDTO;
import com.socialize.rating.service.RatingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ratings")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RatingDTO>> submitRating(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @Valid @RequestBody SubmitRatingRequest request) {
        
        Long raterId = userId != null ? userId : 1L;
        RatingDTO rating = ratingService.submitRating(raterId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Rating submitted successfully", rating));
    }

    @GetMapping("/user/{userId}/score")
    public ResponseEntity<ApiResponse<TrustScoreDTO>> getTrustScore(@PathVariable Long userId) {
        TrustScoreDTO trustScore = ratingService.getTrustScore(userId);
        return ResponseEntity.ok(ApiResponse.success(trustScore));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<RatingDTO>>> getUserRatings(@PathVariable Long userId) {
        List<RatingDTO> ratings = ratingService.getUserRatings(userId);
        return ResponseEntity.ok(ApiResponse.success(ratings));
    }
}
