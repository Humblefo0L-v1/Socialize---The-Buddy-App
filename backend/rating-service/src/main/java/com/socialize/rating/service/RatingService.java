package com.socialize.rating.service;

import com.socialize.common.exception.BadRequestException;
import com.socialize.common.exception.ResourceNotFoundException;
import com.socialize.rating.model.dto.RatingDTO;
import com.socialize.rating.model.dto.SubmitRatingRequest;
import com.socialize.rating.model.dto.TrustScoreDTO;
import com.socialize.rating.model.entity.Rating;
import com.socialize.rating.model.entity.TrustScore;
import com.socialize.rating.repository.RatingRepository;
import com.socialize.rating.repository.TrustScoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class RatingService {

    private static final Logger logger = LoggerFactory.getLogger(RatingService.class);

    private final RatingRepository ratingRepository;
    private final TrustScoreRepository trustScoreRepository;

    public RatingService(RatingRepository ratingRepository, TrustScoreRepository trustScoreRepository) {
        this.ratingRepository = ratingRepository;
        this.trustScoreRepository = trustScoreRepository;
    }

    @Transactional
    public RatingDTO submitRating(Long raterId, SubmitRatingRequest request) {
        if (raterId.equals(request.getRatedUserId())) {
            throw new BadRequestException("Cannot rate yourself");
        }

        if (ratingRepository.existsByRaterIdAndRatedUserIdAndEventId(
                raterId, request.getRatedUserId(), request.getEventId())) {
            throw new BadRequestException("You have already rated this user for this event");
        }

        Rating rating = Rating.builder()
                .raterId(raterId)
                .ratedUserId(request.getRatedUserId())
                .eventId(request.getEventId())
                .score(request.getScore())
                .comment(request.getComment())
                .isAnonymous(request.getIsAnonymous())
                .build();

        rating = ratingRepository.save(rating);
        logger.info("Rating submitted: User {} rated User {} with score {}", raterId, request.getRatedUserId(), request.getScore());

        updateTrustScore(request.getRatedUserId());

        return mapToDTO(rating);
    }

    public TrustScoreDTO getTrustScore(Long userId) {
        TrustScore trustScore = trustScoreRepository.findById(userId)
                .orElse(TrustScore.builder()
                        .userId(userId)
                        .averageRating(BigDecimal.ZERO)
                        .totalRatings(0)
                        .build());

        return mapTrustScoreToDTO(trustScore);
    }

    public List<RatingDTO> getUserRatings(Long userId) {
        List<Rating> ratings = ratingRepository.findByRatedUserId(userId);
        return ratings.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private void updateTrustScore(Long userId) {
        List<Rating> ratings = ratingRepository.findByRatedUserId(userId);
        
        if (ratings.isEmpty()) {
            return;
        }


        double average = ratings.stream()
            .mapToInt(Rating::getScore)
            .average()
            .orElse(0.0);

        TrustScore trustScore = trustScoreRepository.findById(userId)
            .orElse(TrustScore.builder().userId(userId).build());

        BigDecimal avg = BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP);
        trustScore.setAverageRating(avg);
        trustScore.setTotalRatings(ratings.size());

        trustScoreRepository.save(trustScore);
        logger.info("Trust score updated for User {}: {} ({} ratings)", userId, trustScore.getAverageRating(), trustScore.getTotalRatings());
    }

    private RatingDTO mapToDTO(Rating rating) {
        return RatingDTO.builder()
                .id(rating.getId())
                .raterId(rating.getIsAnonymous() ? null : rating.getRaterId())
                .ratedUserId(rating.getRatedUserId())
                .eventId(rating.getEventId())
                .score(rating.getScore())
                .comment(rating.getComment())
                .isAnonymous(rating.getIsAnonymous())
                .createdAt(rating.getCreatedAt())
                .build();
    }

    private TrustScoreDTO mapTrustScoreToDTO(TrustScore trustScore) {
        double avg = trustScore.getAverageRating() != null ? trustScore.getAverageRating().doubleValue() : 0.0;

        String trustLevel;
        if (avg >= 4.5) {
            trustLevel = "EXCELLENT";
        } else if (avg >= 4.0) {
            trustLevel = "VERY_GOOD";
        } else if (avg >= 3.5) {
            trustLevel = "GOOD";
        } else if (avg >= 3.0) {
            trustLevel = "AVERAGE";
        } else {
            trustLevel = "BELOW_AVERAGE";
        }

        return TrustScoreDTO.builder()
                .userId(trustScore.getUserId())
                .averageRating(avg)
                .totalRatings(trustScore.getTotalRatings())
                .trustLevel(trustLevel)
                .build();
    }
}
