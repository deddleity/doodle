package org.doodle.klein.dtos;

import lombok.Data;

@Data
public class TimeSlotResponse {
    private TimeSlotDto timeSlot;
    private String status;
    private String message;
    private String error;
}
