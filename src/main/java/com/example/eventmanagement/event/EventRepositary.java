package com.example.eventmanagement.event;

import com.example.eventmanagement.venue.Venue;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepositary extends JpaRepository<Event,Long> {
    List<Event> findByStatus(EventStatus status);

    List<Event> findByStartTimeAfterOrderByStartTime(LocalDateTime from);
}
