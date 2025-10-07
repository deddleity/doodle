package org.doodle.klein.dtos;

import lombok.Data;

@Data
public class TimeSlotDto {
    private String id;
    private String startTime;
    private String endTime;
}
