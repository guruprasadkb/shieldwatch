package com.shieldwatch.service;

import com.shieldwatch.model.Incident;
import com.shieldwatch.model.enums.Severity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EscalationService {

    public void calculateDeadlines(Incident incident) {
        LocalDateTime now = incident.getCreatedAt() != null ? incident.getCreatedAt() : LocalDateTime.now();

        switch (incident.getSeverity()) {
            case CRITICAL -> {
                incident.setTriageDeadline(now.plusMinutes(15));
                incident.setResolutionDeadline(now.plusHours(4));
            }
            case HIGH -> {
                incident.setTriageDeadline(now.plusHours(1));
                incident.setResolutionDeadline(now.plusHours(4));
            }
            case MEDIUM -> {
                incident.setTriageDeadline(now.plusHours(4));
                incident.setResolutionDeadline(now.plusHours(72));
            }
            case LOW -> {
                // No SLA for LOW severity
                incident.setTriageDeadline(null);
                incident.setResolutionDeadline(null);
            }
        }
    }
}
