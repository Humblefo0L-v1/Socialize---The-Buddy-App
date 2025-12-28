package com.socialize.event.repository;

import com.socialize.event.model.entity.EventParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventParticipantRepository extends JpaRepository<EventParticipant, Long> {

    Optional<EventParticipant> findByEventIdAndUserId(Long eventId, Long userId);

    List<EventParticipant> findByEventId(Long eventId);

    List<EventParticipant> findByUserId(Long userId);

    @Query("SELECT COUNT(p) FROM EventParticipant p WHERE p.event.id = :eventId AND p.status = :status")
    Long countByEventIdAndStatus(@Param("eventId") Long eventId, 
                                 @Param("status") EventParticipant.ParticipantStatus status);

    Boolean existsByEventIdAndUserId(Long eventId, Long userId);

    List<EventParticipant> findByEventIdAndStatus(Long eventId, EventParticipant.ParticipantStatus status);
}

