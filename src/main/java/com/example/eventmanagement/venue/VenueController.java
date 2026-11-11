package com.example.eventmanagement.venue;

import com.example.eventmanagement.controller.GenericController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/venues")
public class VenueController extends GenericController<Venue,VenueService> {
    public VenueController(VenueService venueService){
        super(venueService);
    }

}
