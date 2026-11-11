package com.example.eventmanagement.attendee;

import com.example.eventmanagement.controller.GenericController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendees")
public class AttendeeController extends GenericController<Attendee,AttendeeService> {
    public AttendeeController(AttendeeService service) {
        super(service);
    }
}
