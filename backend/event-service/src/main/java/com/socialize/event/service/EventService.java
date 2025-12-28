package com.socialize.event.service;

import com.socialize.common.dto.LocationDTO;
import com.socialize.common.exception.BadRequestException;
import com.socialize.common.exception.ResourceNotFoundException;
import com.socialize.event.model.dto.*;
import com.socialize.event.model.entity.Event;
import com.socialize.event.model.entity.EventParticipant;
import com.socialize.event.repository.EventParticipantRepository;
import com.socialize.event.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventService {

    private static final Logger logger = LoggerFactory.getLogger(EventService.class);

    private final EventRepository eventRepository;
    private final EventParticipantRepository participantRepository;
    private final KafkaTemplate<String, EventMessage> kafkaTemplate;

    public EventService(EventRepository eventRepository,
                       EventParticipantRepository participantRepository,
                       KafkaTemplate<String, EventMessage> kafkaTemplate) {
        this.eventRepository = eventRepository;
        this.participantRepository = participantRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public EventDTO createEvent(Long hostId, CreateEventRequest request) {
        validateEventTimes(request.getStartTime(), request.getEndTime());

        Event event = Event.builder()
                .hostId(hostId)
                .title(request.getTitle())
                .description(request.getDescription())
                .categoryId(request.getCategoryId())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .address(request.getAddress())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .maxParticipants(request.getMaxParticipants())
                .requirements(request.getRequirements())
                .status(Event.EventStatus.UPCOMING)
                .build();

        event = eventRepository.save(event);
        logger.info("Event created with ID: {}", event.getId());

        // Publish event created message to Kafka
        publishEventMessage(event.getId(), hostId, event.getTitle(), "EVENT_CREATED", null, null);

        return mapToDTO(event);
    }

    public EventDTO getEventById(Long eventId) {
        Event event = eventRepository.findByIdWithParticipants(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        return mapToDTO(event);
    }

    public Page<EventDTO> getEventsByHost(Long hostId, Pageable pageable) {
        return eventRepository.findByHostId(hostId, pageable)
                .map(this::mapToDTO);
    }

    public Page<EventDTO> getUpcomingEvents(Pageable pageable) {
        return eventRepository.findUpcomingEvents(Event.EventStatus.UPCOMING, LocalDateTime.now(), pageable)
                .map(this::mapToDTO);
    }

    public List<EventDTO> getNearbyEvents(Double latitude, Double longitude, Double radiusKm) {
        List<Event> events = eventRepository.findNearbyEvents(latitude, longitude, radiusKm);
        return events.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public EventDTO updateEvent(Long eventId, Long hostId, UpdateEventRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (!event.getHostId().equals(hostId)) {
            throw new BadRequestException("Only the event host can update this event");
        }

        if (event.getStatus() == Event.EventStatus.COMPLETED || 
            event.getStatus() == Event.EventStatus.CANCELLED) {
            throw new BadRequestException("Cannot update a completed or cancelled event");
        }

        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getStartTime() != null && request.getEndTime() != null) {
            validateEventTimes(request.getStartTime(), request.getEndTime());
            event.setStartTime(request.getStartTime());
            event.setEndTime(request.getEndTime());
        }
        if (request.getMaxParticipants() != null) {
            event.setMaxParticipants(request.getMaxParticipants());
        }
        if (request.getRequirements() != null) {
            event.setRequirements(request.getRequirements());
        }

        event = eventRepository.save(event);
        logger.info("Event updated: {}", eventId);

        publishEventMessage(eventId, hostId, event.getTitle(), "EVENT_UPDATED", null, null);

        return mapToDTO(event);
    }

    @Transactional
    public void deleteEvent(Long eventId, Long hostId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (!event.getHostId().equals(hostId)) {
            throw new BadRequestException("Only the event host can delete this event");
        }

        event.setStatus(Event.EventStatus.CANCELLED);
        eventRepository.save(event);
        logger.info("Event cancelled: {}", eventId);

        publishEventMessage(eventId, hostId, event.getTitle(), "EVENT_CANCELLED", null, null);
    }

    @Transactional
    public ParticipantDTO joinEvent(Long eventId, Long userId) {
        Event event = eventRepository.findByIdWithParticipants(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (event.getHostId().equals(userId)) {
            throw new BadRequestException("Host cannot join their own event");
        }

        if (event.getStatus() != Event.EventStatus.UPCOMING) {
            throw new BadRequestException("Can only join upcoming events");
        }

        if (participantRepository.existsByEventIdAndUserId(eventId, userId)) {
            throw new BadRequestException("Already requested to join this event");
        }

        Long approvedCount = participantRepository.countByEventIdAndStatus(
                eventId, EventParticipant.ParticipantStatus.APPROVED);

        if (event.getMaxParticipants() != null && approvedCount >= event.getMaxParticipants()) {
            throw new BadRequestException("Event is full");
        }

        EventParticipant participant = EventParticipant.builder()
                .event(event)
                .userId(userId)
                .status(EventParticipant.ParticipantStatus.PENDING)
                .build();

        participant = participantRepository.save(participant);
        logger.info("User {} requested to join event {}", userId, eventId);

        publishEventMessage(eventId, event.getHostId(), event.getTitle(), "JOIN_REQUEST", userId, "PENDING");

        return mapParticipantToDTO(participant);
    }

    @Transactional
    public ParticipantDTO approveParticipant(Long eventId, Long participantId, Long hostId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (!event.getHostId().equals(hostId)) {
            throw new BadRequestException("Only the event host can approve participants");
        }

        EventParticipant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found"));

        if (!participant.getEvent().getId().equals(eventId)) {
            throw new BadRequestException("Participant does not belong to this event");
        }

        participant.setStatus(EventParticipant.ParticipantStatus.APPROVED);
        participant = participantRepository.save(participant);
        logger.info("Participant {} approved for event {}", participantId, eventId);

        publishEventMessage(eventId, hostId, event.getTitle(), "JOIN_APPROVED", participant.getUserId(), "APPROVED");

        return mapParticipantToDTO(participant);
    }

    @Transactional
    public ParticipantDTO declineParticipant(Long eventId, Long participantId, Long hostId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (!event.getHostId().equals(hostId)) {
            throw new BadRequestException("Only the event host can decline participants");
        }

        EventParticipant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found"));

        if (!participant.getEvent().getId().equals(eventId)) {
            throw new BadRequestException("Participant does not belong to this event");
        }

        participant.setStatus(EventParticipant.ParticipantStatus.DECLINED);
        participant = participantRepository.save(participant);
        logger.info("Participant {} declined for event {}", participantId, eventId);

        publishEventMessage(eventId, hostId, event.getTitle(), "JOIN_DECLINED", participant.getUserId(), "DECLINED");

        return mapParticipantToDTO(participant);
    }

    public List<ParticipantDTO> getEventParticipants(Long eventId) {
        List<EventParticipant> participants = participantRepository.findByEventId(eventId);
        return participants.stream()
                .map(this::mapParticipantToDTO)
                .collect(Collectors.toList());
    }

    public List<ParticipantDTO> getUserEvents(Long userId) {
        List<EventParticipant> participants = participantRepository.findByUserId(userId);
        return participants.stream()
                .map(this::mapParticipantToDTO)
                .collect(Collectors.toList());
    }

    private void validateEventTimes(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Start time must be in the future");
        }
        if (endTime.isBefore(startTime)) {
            throw new BadRequestException("End time must be after start time");
        }
    }

    private void publishEventMessage(Long eventId, Long hostId, String title, 
                                    String type, Long userId, String status) {
        try {
            EventMessage message = EventMessage.builder()
                    .eventId(eventId)
                    .hostId(hostId)
                    .eventTitle(title)
                    .eventType(type)
                    .userId(userId)
                    .status(status)
                    .timestamp(LocalDateTime.now())
                    .build();

            kafkaTemplate.send("event-notifications", message);
            logger.info("Published Kafka message: {}", type);
        } catch (Exception e) {
            logger.error("Failed to publish Kafka message", e);
        }
    }

    private EventDTO mapToDTO(Event event) {
        Long participantCount = participantRepository.countByEventIdAndStatus(
                event.getId(), EventParticipant.ParticipantStatus.APPROVED);

        return EventDTO.builder()
                .id(event.getId())
                .hostId(event.getHostId())
                .title(event.getTitle())
                .description(event.getDescription())
                .categoryId(event.getCategoryId())
                .location(new LocationDTO(event.getLatitude(), event.getLongitude(), event.getAddress()))
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .maxParticipants(event.getMaxParticipants())
                .currentParticipants(participantCount.intValue())
                .requirements(event.getRequirements())
                .status(event.getStatus().name())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }

    private ParticipantDTO mapParticipantToDTO(EventParticipant participant) {
        return ParticipantDTO.builder()
                .id(participant.getId())
                .eventId(participant.getEvent().getId())
                .userId(participant.getUserId())
                .status(participant.getStatus().name())
                .joinedAt(participant.getJoinedAt())
                .build();
    }
}

