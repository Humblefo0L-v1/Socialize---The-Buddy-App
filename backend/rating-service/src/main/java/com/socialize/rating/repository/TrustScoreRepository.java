package com.socialize.rating.repository;

import com.socialize.rating.model.entity.TrustScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrustScoreRepository extends JpaRepository<TrustScore, Long> {
}
