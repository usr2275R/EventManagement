package com.example.eventmanagement.event;

import com.example.eventmanagement.commonPackage.GenericServiceImplementation;
import com.example.eventmanagement.excepition.BusinessRuleException;
import com.example.eventmanagement.excepition.ResourceNotFoundException;
import com.example.eventmanagement.venue.Venue;
import com.example.eventmanagement.venue.VenueRepositary;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Service
public class EventService extends GenericServiceImplementation<Event,EventRepositary> {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);


    private final VenueRepositary venueRepositary;
    private final HolidayService holidayService;


    public EventService(EventRepositary repository, VenueRepositary venueRepositary, HolidayService holidayService) {
        super(repository, "Event");
        this.venueRepositary = venueRepositary;
        this.holidayService = holidayService;


    }

    @Override
    public Event create(Event event) {
        validate(event);
        String warning = holidayWarning(event);
        Event saved = super.create(event);
        saved.setWarning(warning);
        return saved;
    }

    @Override
    public Event update(Long id, Event event) {
        validate(event);
        return super.update(id, event);
    }

    @Override
    protected void copyFields(Event source, Event target) {
        target.setTitle(source.getTitle());
        target.setDescription(source.getDescription());
        target.setStartTime(source.getStartTime());
        target.setEndTime(source.getEndTime());
        target.setCapacity(source.getCapacity());
        target.setStatus(source.getStatus());
        target.setVenue(source.getVenue());
    }

    @Transactional(readOnly = true)
    public List<Event> findByStatus(EventStatus status) {
        return repository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Event> findUpcoming() {
        return repository.findByStartTimeAfterOrderByStartTime(LocalDateTime.now());
    }

    public Event changeStatus(Long id, EventStatus status) {
        Event event = findById(id);
        event.setStatus(status);
        return repository.save(event);
    }

    private void validate(Event event) {
        if (!event.getEndTime().isAfter(event.getStartTime())) {
            throw new BusinessRuleException("Event end time must be after start time");
        }
        if (event.getVenue() != null && event.getVenue().getId() != null) {
            Long venueId = event.getVenue().getId();
            Venue venue = venueRepositary.findById(venueId)
                    .orElseThrow(() -> new ResourceNotFoundException("Venue", venueId));
            if (event.getCapacity() > venue.getCapacity()) {
                throw new BusinessRuleException(
                        "Event capacity exceeds venue capacity (" + venue.getCapacity() + ")");
            }
            event.setVenue(venue);
        } else {
            event.setVenue(null);
        }
    }
    private String holidayWarning(Event event) {
        try {
            LocalDate date = event.getStartTime().toLocalDate();
            return holidayService.getHoliday(date)
                    .map(h -> "Event is scheduled on a public holiday: " + h.name())
                    .orElse(null);
        } catch (Exception e) {
            log.warn("Holiday check failed: {}", e.getMessage());
            return null;
        }
    }

    public List<HolidayService.Holiday> testHolidays(int year, String country) {
        return holidayService.getPublicHolidays(year, country);
    }



}
