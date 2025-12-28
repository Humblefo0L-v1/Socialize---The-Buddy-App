package com.socialize.event.repository;

import com.socialize.event.model.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.participants WHERE e.id = :id")
    Optional<Event> findByIdWithParticipants(@Param("id") Long id);

    Page<Event> findByHostId(Long hostId, Pageable pageable);

    Page<Event> findByStatus(Event.EventStatus status, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.status = :status AND e.startTime > :now")
    Page<Event> findUpcomingEvents(@Param("status") Event.EventStatus status, 
                                   @Param("now") LocalDateTime now, 
                                   Pageable pageable);

    @Query(value = "SELECT * FROM events e WHERE e.status = 'UPCOMING' " +
           "AND (6371 * acos(cos(radians(:latitude)) * cos(radians(e.latitude)) * " +
           "cos(radians(e.longitude) - radians(:longitude)) + sin(radians(:latitude)) * " +
           "sin(radians(e.latitude)))) <= :radiusKm " +
           "ORDER BY (6371 * acos(cos(radians(:latitude)) * cos(radians(e.latitude)) * " +
           "cos(radians(e.longitude) - radians(:longitude)) + sin(radians(:latitude)) * " +
           "sin(radians(e.latitude))))",
           nativeQuery = true)
    List<Event> findNearbyEvents(@Param("latitude") Double latitude,
                                 @Param("longitude") Double longitude,
                                 @Param("radiusKm") Double radiusKm);

    List<Event> findByStatusAndStartTimeBetween(Event.EventStatus status, 
                                                LocalDateTime start, 
                                                LocalDateTime end);
}

