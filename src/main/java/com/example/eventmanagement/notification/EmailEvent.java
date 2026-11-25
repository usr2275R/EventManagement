package com.example.eventmanagement.notification;

import java.util.List;
import java.util.Map;

public record EmailEvent( String requestId,List<String> to, String subject, String templateName, Map<String, Object> variables) {}