package com.shieldwatch.repository;

import com.shieldwatch.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, String> {

    List<AuditLog> findByIncidentIdOrderByTimestampAsc(String incidentId);
}
