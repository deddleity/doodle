package org.doodle.klein.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import jakarta.persistence.GeneratedValue;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "time_slots", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlotEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "time_slot_id", updatable = false, nullable = false)
    private UUID timeSlotId;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "user_id", nullable = false)
    private UUID userId;
}
