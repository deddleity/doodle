package org.doodle.klein.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.doodle.klein.configurations.Authorizaition;
import org.doodle.klein.dtos.TimeSlotDto;
import org.doodle.klein.dtos.TimeSlotResponse;
import org.doodle.klein.entities.TimeSlotEntity;
import org.doodle.klein.services.TimeSlotsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/timeSlots")
public class TimeSlotsController {

    private final TimeSlotsService timeSlotsService;
    private final Authorizaition authorizaition;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("H:mm");

    @PostMapping("/create")
    public ResponseEntity<TimeSlotResponse> setTimeSlot(@RequestBody TimeSlotDto timeSlotDto, HttpServletRequest request) {
        TimeSlotResponse response = new TimeSlotResponse();
        try {
            var userId = authorizaition.resolveUserId(request);
            TimeSlotEntity saved = timeSlotsService.createTimeSlot(timeSlotDto, userId);
            TimeSlotDto dto = new TimeSlotDto();
            dto.setId(saved.getTimeSlotId().toString());
            dto.setStartTime(saved.getStartTime().format(TIME_FMT));
            dto.setEndTime(saved.getEndTime().format(TIME_FMT));

            response.setTimeSlot(dto);
            response.setStatus("OK");
            response.setMessage("Time slot created successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating time slot: {}", e.getMessage(), e);
            response.setStatus("ERROR");
            response.setError(e.getMessage());
            response.setMessage("Failed to create time slot");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/update")
    public ResponseEntity<TimeSlotResponse> updateTimeSlot(@RequestBody TimeSlotDto timeSlotDto, HttpServletRequest request) {
        TimeSlotResponse response = new TimeSlotResponse();
        try {
            var userId = authorizaition.resolveUserId(request);
            TimeSlotEntity saved = timeSlotsService.updateTimeSlot(timeSlotDto, userId);
            TimeSlotDto dto = new TimeSlotDto();
            dto.setId(saved.getTimeSlotId().toString());
            dto.setStartTime(saved.getStartTime().format(TIME_FMT));
            dto.setEndTime(saved.getEndTime().format(TIME_FMT));

            response.setTimeSlot(dto);
            response.setStatus("OK");
            response.setMessage("Time slot updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating time slot: {}", e.getMessage(), e);
            response.setStatus("ERROR");
            response.setError(e.getMessage());
            response.setMessage("Failed to update time slot");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/delete")
    public ResponseEntity<TimeSlotResponse> deleteTimeSlot(@RequestBody TimeSlotDto timeSlotDto, HttpServletRequest request) {
        TimeSlotResponse response = new TimeSlotResponse();
        try {
            var userId = authorizaition.resolveUserId(request);
            var id = UUID.fromString(timeSlotDto.getId());
            timeSlotsService.deleteTimeSlot(id, userId);
            response.setStatus("OK");
            response.setMessage("Time slot deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error deleting time slot: {}", e.getMessage(), e);
            response.setStatus("ERROR");
            response.setError(e.getMessage());
            response.setMessage("Failed to delete time slot");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/statusToggle")
    public ResponseEntity<TimeSlotResponse> toggleStatus(@RequestBody TimeSlotDto timeSlotDto, HttpServletRequest request) {
        TimeSlotResponse response = new TimeSlotResponse();
        try {
            var userId = authorizaition.resolveUserId(request);
            var id = UUID.fromString(timeSlotDto.getId());
            boolean enabled = timeSlotsService.toggleTimeSlotStatus(id, userId);

            TimeSlotDto dto = new TimeSlotDto();
            dto.setId(id.toString());
            dto.setStatusId(enabled);

            response.setTimeSlot(dto);
            response.setStatus("OK");
            response.setMessage("Time slot state toggled successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error toggling time slot state: {}", e.getMessage(), e);
            response.setStatus("ERROR");
            response.setError(e.getMessage());
            response.setMessage("Failed to toggle time slot state");
            return ResponseEntity.badRequest().body(response);
        }
    }
}
