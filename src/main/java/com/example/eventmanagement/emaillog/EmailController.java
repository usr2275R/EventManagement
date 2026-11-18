package com.example.eventmanagement.emaillog;


import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/email-logs")
public class EmailController {

    private final EmailRepository emailRepository;

    public EmailController(EmailRepository emailRepository) {
        this.emailRepository = emailRepository;
    }

    @GetMapping
    public List<EmailLog> list(@RequestParam(required = false) String status,
                               @RequestParam(required = false) String subject){
        if(status != null){
            return emailRepository.findByStatus(status.toUpperCase());
        }

        if(subject != null){
            return emailRepository.findBySubject(subject);
        }
        return emailRepository.findAll(Sort.by(Sort.Direction.DESC,"requestedAt"));
    }
    @GetMapping("/stats")
    public EmailStats stats(){
        return new EmailStats(emailRepository.count(),
                emailRepository.countByStatus("SENT"),
                emailRepository.countByStatus("FAILED"),
                emailRepository.findAvg());
    }
}
