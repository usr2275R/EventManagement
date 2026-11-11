package com.example.eventmanagement.registration;

import com.example.eventmanagement.attendee.Attendee;
import com.example.eventmanagement.attendee.AttendeeRepositary;
import com.example.eventmanagement.commonPackage.GenericServiceImplementation;
import com.example.eventmanagement.event.Event;
import com.example.eventmanagement.event.EventRepositary;
import com.example.eventmanagement.event.EventStatus;
import com.example.eventmanagement.excepition.BusinessRuleException;
import com.example.eventmanagement.excepition.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RegistrationService extends GenericServiceImplementation<Registration,RegistrationRepository> {
    private final EventRepositary eventRepositary;
    private final AttendeeRepositary attendeeRepositary;

    public RegistrationService(RegistrationRepository repository,
                               EventRepositary eventRepositary,
                               AttendeeRepositary attendeeRepositary) {
        super(repository, "Registration");
        this.eventRepositary = eventRepositary;
        this.attendeeRepositary = attendeeRepositary;
    }

    @Override
    public Registration create(Registration registration){
        Long eventId = registration.getEvent().getId();
        Long attendeeId= registration.getAttendee().getId();
        if(eventId==null || attendeeId==null){
            throw new BusinessRuleException("Both event_id and attendee_id are required");
        }

        Event event =eventRepositary.findById(eventId)
                .orElseThrow(()-> new ResourceNotFoundException("Event",eventId));
        Attendee attendee =attendeeRepositary.findById(attendeeId)
                .orElseThrow(()-> new ResourceNotFoundException("Attendee",attendeeId));

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new BusinessRuleException("Registration is only open for PUBLISHED events");
        }
        if (repository.existsByEventIdAndAttendeeId(eventId, attendeeId)) {
            throw new BusinessRuleException("Attendee is already registered for this event");
        }
        if (repository.countByEventId(eventId) >= event.getCapacity()) {
            throw new BusinessRuleException("Event is full");
        }
        registration.setEvent(event);
        registration.setAttendee(attendee);
        registration.setCheckedIn(false);
        return super.create(registration);

    }

    @Override
    protected void copyFields(Registration source, Registration target) {
        target.setCheckedIn(source.isCheckedIn());
    }

    @Transactional(readOnly = true)
    public List<Registration> findByEvent(Long eventId) {
        return repository.findByEventId(eventId);
    }

    @Transactional(readOnly = true)
    public List<Registration> findByAttendee(Long attendeeId) {
        return repository.findByAttendeeId(attendeeId);
    }

    public Registration checkIn(Long id) {
        Registration registration = findById(id);
        registration.setCheckedIn(true);
        return repository.save(registration);
    }
}
