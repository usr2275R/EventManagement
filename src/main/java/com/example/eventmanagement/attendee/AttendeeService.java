package com.example.eventmanagement.attendee;

import com.example.eventmanagement.commonPackage.GenericServiceImplementation;
import org.springframework.stereotype.Service;

@Service
public class AttendeeService extends GenericServiceImplementation<Attendee,AttendeeRepositary> {
    public AttendeeService(AttendeeRepositary attendeeRepository) {
        super(attendeeRepository, "Attendee");
    }

    @Override
    protected void copyFields(Attendee source, Attendee target) {
        target.setName(source.getName());
        target.setEmail(source.getEmail());
        target.setPhone(source.getPhone());
    }
}
