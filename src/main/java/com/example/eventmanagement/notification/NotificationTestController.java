package com.example.eventmanagement.notification;

import com.example.eventmanagement.event.Event;
import com.example.eventmanagement.event.EventService;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@RestController
public class NotificationTestController {

    private static final Logger log = LoggerFactory.getLogger(NotificationTestController.class);

    @Autowired
    private final KafkaTemplate<String, EmailEvent> kafkaTemplate;

    private final EventService eventService;

    public NotificationTestController(KafkaTemplate<String, EmailEvent> kafkaTemplate,
                                      EventService eventService) {
        this.kafkaTemplate = kafkaTemplate;
        this.eventService = eventService;
    }

    @PostMapping("/test/bulk-email")
    public String bulk(@RequestParam(defaultValue = "50") int count) {
        for (int i = 1; i <= count; i++) {
            EmailEvent emailEvent = new EmailEvent(
                    "chandru4809+" + i + "@gmail.com", "Test " + i, "Body " + i);
            kafkaTemplate.send("email-events", emailEvent.to(), emailEvent);
        }
        kafkaTemplate.flush();
        return "Queued " + count + " emails";
    }

    @PostMapping(value = "/test/send-csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String sendFromCsv(@RequestParam("file") MultipartFile file,
                              @RequestParam Long eventId) throws IOException {
        Event event = eventService.findById(eventId);
        int sent = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;

                String[] parts = line.split(",", 2);
                String email = parts[0].trim();
                String name = parts.length > 1 ? parts[1].trim() : "there";

                EmailEvent emailEvent = new EmailEvent(
                        email,
                        "Registration Confirmation: " + event.getTitle(),
                        "Hi " + name + ",\n\nYou have been registered successfully for "
                                + event.getTitle() + ".");

                kafkaTemplate.send("email-events", email, emailEvent)
                        .whenComplete((result, ex) -> {
                            if (ex != null) {
                                log.error("Kafka send FAILED for {}", email, ex);
                            } else {
                                log.info("Kafka accepted {} -> partition {} offset {}", email,
                                        result.getRecordMetadata().partition(),
                                        result.getRecordMetadata().offset());
                            }
                        });
                sent++;
            }
        }
        kafkaTemplate.flush();
        log.info("CSV request handled, queued {} emails for event {}", sent, eventId);
        return "Queued " + sent + " emails";
    }


    @PostMapping("/test/trigger-dlt")
    public String triggerDlt() {
        EmailEvent event = new EmailEvent(
                "uday2275@zohomail.in",
                "Test failed email",
                "Test body");

        ProducerRecord<String, EmailEvent> record =
                new ProducerRecord<>("email-events.DLT", event.to(), event);
        record.headers().add(KafkaHeaders.DLT_EXCEPTION_MESSAGE,
                "Manually triggered test failure".getBytes(StandardCharsets.UTF_8));

        kafkaTemplate.send(record);
        log.info("Manually sent test event to email-events.DLT");
        return "Sent to DLT";
    }
}