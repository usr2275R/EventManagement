package com.example.eventmanagement.notification;

import com.example.eventmanagement.emaillog.EmailLog;
import com.example.eventmanagement.emaillog.EmailRepository;
import jakarta.persistence.Column;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class EmailEventListener {

    private static final Logger log = LoggerFactory.getLogger(EmailEventListener.class);
    private final JavaMailSender mailSender;
    private final String adminEmail;
    private final String fromAddress;
    private final EmailRepository emailRepository;

    public EmailEventListener(JavaMailSender mailSender,
                              EmailRepository emailRepository,
                              @Value("${app.admin.email}") String adminEmail,
                              @Value("${spring.mail.username}") String fromAddress) {
        this.mailSender = mailSender;
        this.adminEmail = adminEmail;
        this.emailRepository=emailRepository;
        this.fromAddress = fromAddress;
    }
    @KafkaListener(topics = "email-events", groupId = "email-service")
    public void onEmailEvent(EmailEvent event) {
        EmailLog emailLog = new EmailLog();
        emailLog.setRecipient(event.to());
        emailLog.setSubject(event.subject());
        emailLog.setRequestedAt(LocalDateTime.now());

        try {
            if (event.to().contains("fail")) {
                throw new RuntimeException("Simulated failure for " + event.to());
            }

            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(event.to());
            msg.setFrom(fromAddress);
            msg.setSubject(event.subject());
            msg.setText(event.body());
            mailSender.send(msg);

            emailLog.setStatus("SENT");
            log.info("MAIL SENT to={} from {} subject={} at={}", event.to(), fromAddress, event.subject(), LocalDateTime.now());
        }
        catch (RuntimeException e){
            emailLog.setStatus("FAILED");
            emailLog.setErrorResponse(e.getMessage());
            log.warn("Mail failed to {}: {}", event.to(),e.getMessage());
            throw e;
        }
        finally {
            emailLog.setRespondedAt(LocalDateTime.now());
            emailLog.setDuration(Duration.between(emailLog.getRequestedAt(), emailLog.getRespondedAt()).toMillis());
            emailRepository.save(emailLog);
        }
    }


    @KafkaListener(topics = "email-events.DLT", groupId = "dlt-group")
    public void consumeDlt(EmailEvent event,
                           @Header(KafkaHeaders.DLT_EXCEPTION_MESSAGE) String error) {
        log.error("Dead letter received: {} | reason: {}", event.to(), error);

        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromAddress);
            msg.setTo(adminEmail);
            msg.setSubject("Email delivery failed: " + event.subject());
            msg.setText("""
                An email could not be delivered after all retries.

                Recipient: %s
                Subject:   %s
                Error:     %s
                """.formatted(event.to(), event.subject(), error));
            mailSender.send(msg);
            log.info("Admin notified about failed email to {}", event.to());
        } catch (Exception e) {

            log.error("Could not notify admin about failed email to {}", event.to(), e);
        }
    }


}
