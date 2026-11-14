package com.example.eventmanagement.registration;

import com.example.eventmanagement.attendee.Attendee;
import com.example.eventmanagement.controller.GenericController;
import com.example.eventmanagement.event.Event;
import com.example.eventmanagement.event.EventService;
import com.example.eventmanagement.excepition.ResourceNotFoundException;
import com.example.eventmanagement.notification.EmailEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

@RestController
@RequestMapping("/api/registrations")
public class RegistrationController extends GenericController<Registration,RegistrationService> {

    private final ApplicationEventPublisher publisher;
    private final EventService eventService;


    public RegistrationController(RegistrationService service,ApplicationEventPublisher publisher,EventService eventService) {
        super(service);
        this.publisher = publisher;
        this.eventService = eventService;



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
    @PostMapping("/send-mail")
    public ResponseEntity<String> sendMail(@RequestParam String to,
                                           @RequestParam String name,
                                           @RequestParam Long eventId) {
        Event event = eventService.findById(eventId);

        publisher.publishEvent(new EmailEvent(
                to,
                "Registration Confirmation: " + event.getTitle(),
                "Hi " +name +",\n"
                        +" you have been registered successfully for " + event.getTitle() + "."));
        return ResponseEntity.ok("Mail event published");
    }


}
