package com.shieldwatch.service;

import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    public void notifyAssignment(String incidentId, String assigneeId) {
        // Notification dispatch placeholder
    }

    public void notifyEscalation(String incidentId, String reason) {
        // Notification dispatch placeholder
    }

    public void notifyStatusChange(String incidentId, String oldStatus, String newStatus) {
        // Notification dispatch placeholder
    }
}
