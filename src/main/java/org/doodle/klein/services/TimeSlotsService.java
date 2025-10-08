package org.doodle.klein.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.doodle.klein.dtos.TimeSlotDto;
import org.doodle.klein.entities.TimeSlotEntity;
import org.doodle.klein.entities.TimeSlotStateEntity;
import org.doodle.klein.repositories.TimeSlotStateRepository;
import org.doodle.klein.repositories.TimeSlotsRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimeSlotsService {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("H:mm");
    private static final String STATE_ENABLED = "ENABLED";
    private static final String STATE_DISABLED = "DISABLED";

    private final TimeSlotsRepository timeSlotRepository;
    private final TimeSlotStateRepository timeSlotStateRepository;

    public TimeSlotEntity createTimeSlot(TimeSlotDto timeSlotDto, UUID userId) {
        try {
            LocalDate today = LocalDate.now();
            LocalTime start = parseTime(timeSlotDto.getStartTime());
            LocalTime end = parseTime(timeSlotDto.getEndTime());
            LocalDateTime startDt = LocalDateTime.of(today, start);
            LocalDateTime endDt = LocalDateTime.of(today, end);

            if (!endDt.isAfter(startDt)) {
                throw new IllegalArgumentException("endTime must be after startTime");
            }

            TimeSlotEntity entity = TimeSlotEntity.builder()
                    .userId(userId)
                    .startTime(startDt)
                    .endTime(endDt)
                    .build();

            TimeSlotEntity saved = timeSlotRepository.save(entity);
            log.info("Created time slot id={} for userId={} from {} to {}", saved.getTimeSlotId(), userId, saved.getStartTime(), saved.getEndTime());
            return saved;
        } catch (DateTimeParseException dtpe) {
            log.error("Invalid time format provided: start='{}', end='{}'", timeSlotDto.getStartTime(), timeSlotDto.getEndTime(), dtpe);
            throw dtpe;
        } catch (Exception e) {
            log.error("Failed to create time slot for userId={}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    public TimeSlotEntity updateTimeSlot(TimeSlotDto timeSlotDto, UUID userId) {
        try {
            UUID id = UUID.fromString(timeSlotDto.getId());
            Optional<TimeSlotEntity> existingOpt = timeSlotRepository.findById(id);
            TimeSlotEntity existing = existingOpt.orElseThrow(() -> new IllegalArgumentException("Time slot not found"));
            if (!existing.getUserId().equals(userId)) {
                throw new IllegalArgumentException("Forbidden: time slot does not belong to user");
            }
            LocalDate date = existing.getStartTime().toLocalDate();
            LocalTime newStart = parseTime(timeSlotDto.getStartTime());
            LocalTime newEnd = parseTime(timeSlotDto.getEndTime());
            LocalDateTime startDt = LocalDateTime.of(date, newStart);
            LocalDateTime endDt = LocalDateTime.of(date, newEnd);
            if (!endDt.isAfter(startDt)) {
                throw new IllegalArgumentException("endTime must be after startTime");
            }
            existing.setStartTime(startDt);
            existing.setEndTime(endDt);
            TimeSlotEntity saved = timeSlotRepository.save(existing);
            log.info("Updated time slot id={} for userId={} to {}-{}", saved.getTimeSlotId(), userId, saved.getStartTime(), saved.getEndTime());
            return saved;
        } catch (Exception e) {
            log.error("Failed to update time slot for userId={}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    public void deleteTimeSlot(UUID timeSlotId, UUID userId) {
        try {
            TimeSlotEntity existing = timeSlotRepository.findById(timeSlotId)
                    .orElseThrow(() -> new IllegalArgumentException("Time slot not found"));
            if (!existing.getUserId().equals(userId)) {
                throw new IllegalArgumentException("Forbidden: time slot does not belong to user");
            }
            timeSlotRepository.delete(existing);
            log.info("Deleted time slot id={} for userId={}", timeSlotId, userId);
        } catch (Exception e) {
            log.error("Failed to delete time slot id={} for userId={}: {}", timeSlotId, userId, e.getMessage(), e);
            throw e;
        }
    }

    public boolean toggleTimeSlotStatus(UUID timeSlotId, UUID userId) {
        try {
            // Validate ownership
            TimeSlotEntity existing = timeSlotRepository.findById(timeSlotId)
                    .orElseThrow(() -> new IllegalArgumentException("Time slot not found"));
            if (!existing.getUserId().equals(userId)) {
                throw new IllegalArgumentException("Forbidden: time slot does not belong to user");
            }

            Optional<TimeSlotStateEntity> stateOpt = timeSlotStateRepository.findById(timeSlotId);
            String newState;
            if (stateOpt.isPresent()) {
                var current = stateOpt.get();
                newState = STATE_ENABLED.equalsIgnoreCase(current.getState()) ? STATE_DISABLED : STATE_ENABLED;
                current.setState(newState);
                timeSlotStateRepository.save(current);
            } else {
                // Default to DISABLED when absent; toggling makes it ENABLED
                newState = STATE_ENABLED;
                TimeSlotStateEntity toSave = TimeSlotStateEntity.builder()
                        .timeSlotId(timeSlotId)
                        .state(newState)
                        .build();
                timeSlotStateRepository.save(toSave);
            }
            boolean enabled = STATE_ENABLED.equalsIgnoreCase(newState);
            log.info("Toggled state for time slot id={} to {} (enabled={})", timeSlotId, newState, enabled);
            return enabled;
        } catch (Exception e) {
            log.error("Failed to toggle status for time slot id={} and userId={}: {}", timeSlotId, userId, e.getMessage(), e);
            throw e;
        }
    }

    private LocalTime parseTime(String timeStr) {
        if (timeStr == null) {
            throw new IllegalArgumentException("time must not be null");
        }
        return LocalTime.parse(timeStr, TIME_FMT);
    }
}
