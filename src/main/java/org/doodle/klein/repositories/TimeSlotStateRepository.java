package org.doodle.klein.repositories;

import org.doodle.klein.entities.TimeSlotStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TimeSlotStateRepository extends JpaRepository<TimeSlotStateEntity, UUID> {
}
