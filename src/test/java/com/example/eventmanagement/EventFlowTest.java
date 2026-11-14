
package com.example.eventmanagement;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EventFlowTest {

    @Autowired
    MockMvc mvc;

    @Test
    void venueEventAttendeeRegistrationFlow() throws Exception {
        mvc.perform(post("/api/venues").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Hall A\",\"address\":\"1 Main St\",\"capacity\":100}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        mvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Spring Meetup","capacity":1,"status":"PUBLISHED",
                                 "startTime":"2030-01-01T10:00:00","endTime":"2030-01-01T12:00:00",
                                 "venue":{"id":1}}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.venue.name").value("Hall A"));

        mvc.perform(post("/api/attendees").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Asha\",\"email\":\"asha@example.com\"}"))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/attendees").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ravi\",\"email\":\"ravi@example.com\"}"))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/registrations").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"event\":{\"id\":1},\"attendee\":{\"id\":1}}"))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/registrations").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"event\":{\"id\":1},\"attendee\":{\"id\":2}}"))
                .andExpect(status().isConflict());

        mvc.perform(get("/api/events/99")).andExpect(status().isNotFound());
    }
}