package com.shieldwatch.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    private String id;

    @PrePersist
    protected void ensureId() {
        if (this.id == null) this.id = java.util.UUID.randomUUID().toString();
    }

    @Column(nullable = false)
    private String incidentId;

    @Column(nullable = false)
    private String performedBy;

    @Column(nullable = false)
    private String action;

    private String oldValue;

    private String newValue;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
