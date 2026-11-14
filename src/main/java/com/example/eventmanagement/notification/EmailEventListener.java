package com.example.eventmanagement.notification;

import jakarta.persistence.Column;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class EmailEventListener {

    private static final Logger log = LoggerFactory.getLogger(EmailEventListener.class);
    private final JavaMailSender mailSender;

    public EmailEventListener(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    @EventListener
    public void onEmailEvent(EmailEvent event) {
        log.info("Sending email to {}", event.to());
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom("chandru4809@gmail.com");
            msg.setTo(event.to());
            msg.setSubject(event.subject());
            msg.setText(event.body());
            mailSender.send(msg);
        }catch (Exception e) {
            log.error("Failed to send email to {}", event.to(), e);
        }
    }
}
