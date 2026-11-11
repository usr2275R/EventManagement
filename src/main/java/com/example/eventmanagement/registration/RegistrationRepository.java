package com.example.eventmanagement.registration;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegistrationRepository extends JpaRepository<Registration,Long> {

    long countByEventId(Long eventId);

    boolean existsByEventIdAndAttendeeId(Long eventId, Long attendeeId);

    List<Registration> findByEventId(Long eventId);

    List<Registration> findByAttendeeId(Long attendeeId);
}
