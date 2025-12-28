package com.socialize.rating.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ratings",
    uniqueConstraints = @UniqueConstraint(columnNames = {"rater_id", "rated_user_id", "event_id"}),
    indexes = {
        @Index(name = "idx_rated_user", columnList = "rated_user_id"),
        @Index(name = "idx_event", columnList = "event_id")
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rater_id", nullable = false)
    private Long raterId;

    @Column(name = "rated_user_id", nullable = false)
    private Long ratedUserId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private Integer score;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "is_anonymous", nullable = false)
    private Boolean isAnonymous = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
