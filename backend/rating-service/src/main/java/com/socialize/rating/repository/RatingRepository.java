package com.socialize.rating.repository;

import com.socialize.rating.model.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    List<Rating> findByRatedUserId(Long ratedUserId);

    List<Rating> findByRaterIdAndRatedUserId(Long raterId, Long ratedUserId);

    Optional<Rating> findByRaterIdAndRatedUserIdAndEventId(Long raterId, Long ratedUserId, Long eventId);

    Boolean existsByRaterIdAndRatedUserIdAndEventId(Long raterId, Long ratedUserId, Long eventId);

    Long countByRatedUserId(Long ratedUserId);
}
