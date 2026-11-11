package com.example.eventmanagement.registration;

import com.example.eventmanagement.controller.GenericController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/registrations")
public class RegistrationController extends GenericController<Registration,RegistrationService> {

    public RegistrationController(RegistrationService service) {
        super(service);
    }

    @GetMapping("/event/{eventId}")
    public List<Registration> byEvent(@PathVariable Long eventId) {
        return service.findByEvent(eventId);
    }

    @GetMapping("/attendee/{attendeeId}")
    public List<Registration> byAttendee(@PathVariable Long attendeeId) {
        return service.findByAttendee(attendeeId);
    }

    @PatchMapping("/{id}/check-in")
    public Registration checkIn(@PathVariable Long id) {
        return service.checkIn(id);
    }

}
