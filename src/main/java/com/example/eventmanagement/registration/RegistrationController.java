package com.example.eventmanagement.registration;

import com.example.eventmanagement.attendee.Attendee;
import com.example.eventmanagement.controller.GenericController;
import com.example.eventmanagement.event.Event;
import com.example.eventmanagement.event.EventService;
import com.example.eventmanagement.excepition.ResourceNotFoundException;
import com.example.eventmanagement.notification.EmailEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/registrations")
public class RegistrationController extends GenericController<Registration,RegistrationService> {

    private final KafkaTemplate<String, EmailEvent> kafkaTemplate;
    private final EventService eventService;


    public RegistrationController(RegistrationService service,
                                  KafkaTemplate<String, EmailEvent> kafkaTemplate,
                                  EventService eventService) {
        super(service);
        this.kafkaTemplate = kafkaTemplate;
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

//    @PostMapping("/send-mail")
//    public ResponseEntity<EmailEvent> sendMail(@RequestParam String to,
//                                           @RequestParam String name,
//                                           @RequestParam Long eventId) {
//        Event event = eventService.findById(eventId);
//
//        Map<String, Object> variables = new HashMap<>();
//        variables.put("userName", name);
//        variables.put("eventTitle", event.getTitle());
//
//        EmailEvent emailEvent = new EmailEvent(
//                UUID.randomUUID().toString(),
//                List.of(to),
//                "Registration Confirmation: " + event.getTitle(),
//                "Event Register",
//                variables
//        );
//
//        kafkaTemplate.send("email-request-topic", to, emailEvent);
//
//        return ResponseEntity.ok("Mail event queued");
//    }


@PostMapping("/send-event")
public ResponseEntity<String> sendEvent(@RequestBody EmailEvent incoming) {

        //error while accepting the templatename in another app becoz here it is sending defult one

    if (incoming.templateName() == null || incoming.templateName().isBlank()) {
        return ResponseEntity.badRequest().body("templateName is required");
    }
    if (incoming.to() == null || incoming.to().isEmpty()) {
        return ResponseEntity.badRequest().body("to is required");
    }

    // requestId is mandatory in emailService (unique), so generate one if the caller omits it
    String requestId = (incoming.requestId() == null || incoming.requestId().isBlank())
            ? UUID.randomUUID().toString()
            : incoming.requestId();

    EmailEvent event = new EmailEvent(
            requestId,
            incoming.to(),
            incoming.subject(),
            incoming.templateName(),
            incoming.variables()
    );

    kafkaTemplate.send("email-request-topic", event.to().get(0), event);

    return ResponseEntity.ok("Mail event queued: ");
}


}
