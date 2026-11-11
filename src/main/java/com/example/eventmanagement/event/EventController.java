package com.example.eventmanagement.event;

import com.example.eventmanagement.controller.GenericController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController extends GenericController<Event,EventService> {
    public EventController(EventService service) {
        super(service);
    }
    @GetMapping("/upcoming")
    public List<Event> upcoming() {
        return service.findUpcoming();
    }

    @GetMapping("/status/{status}")
    public List<Event> byStatus(@PathVariable EventStatus status) {
        return service.findByStatus(status);
    }

    @PatchMapping("/{id}/status")
    public Event changeStatus(@PathVariable Long id, @RequestParam EventStatus value) {
        return service.changeStatus(id, value);
    }

}
