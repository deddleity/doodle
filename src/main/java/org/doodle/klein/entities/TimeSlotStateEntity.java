package org.doodle.klein.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "time_slot_state", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlotStateEntity {

    @Id
    @Column(name = "time_slot_id", nullable = false, updatable = false)
    private UUID timeSlotId;

    @Column(name = "state", nullable = false)
    private String state;
}
