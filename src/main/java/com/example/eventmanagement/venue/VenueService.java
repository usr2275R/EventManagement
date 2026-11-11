package com.example.eventmanagement.venue;

import com.example.eventmanagement.commonPackage.GenericServiceImplementation;
import org.springframework.stereotype.Service;


@Service
public class VenueService extends GenericServiceImplementation<Venue,VenueRepositary> {
    public VenueService(VenueRepositary venueRepositary){
        super(venueRepositary,"venue");
    }

    @Override
    protected void copyFields(Venue source,Venue target){
        target.setName(source.getName());
        target.setAddress(source.getAddress());
        target.setCapacity(source.getCapacity());
    }
}
