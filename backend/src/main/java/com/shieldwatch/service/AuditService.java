package com.shieldwatch.service;

import com.shieldwatch.model.AuditLog;
import com.shieldwatch.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String incidentId, String performedBy, String action, String oldValue, String newValue) {
        AuditLog entry = AuditLog.builder()
                .incidentId(incidentId)
                .performedBy(performedBy)
                .action(action)
                .oldValue(oldValue)
                .newValue(newValue)
                .build();
        auditLogRepository.save(entry);
    }

    public List<AuditLog> getAuditTrail(String incidentId) {
        return auditLogRepository.findByIncidentIdOrderByTimestampAsc(incidentId);
    }
}
