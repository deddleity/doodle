package org.doodle.klein.repositories;

import org.doodle.klein.entities.TimeSlotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TimeSlotsRepository extends JpaRepository<TimeSlotEntity, UUID> {
    List<TimeSlotEntity> findByUserId(UUID userId);
}
