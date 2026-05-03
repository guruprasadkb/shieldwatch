package com.shieldwatch.dto;

import com.shieldwatch.model.Incident;
import com.shieldwatch.model.enums.Severity;
import com.shieldwatch.model.enums.Status;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class IncidentResponse {
    private String id;
    private String title;
    private String description;
    private Severity severity;
    private Status status;
    private String reporterUsername;
    private String assigneeUsername;
    private String teamName;
    private String teamId;
    private LocalDateTime triageDeadline;
    private LocalDateTime resolutionDeadline;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static IncidentResponse from(Incident incident) {
        return IncidentResponse.builder()
                .id(incident.getId())
                .title(incident.getTitle())
                .description(incident.getDescription())
                .severity(incident.getSeverity())
                .status(incident.getStatus())
                .reporterUsername(incident.getReporter() != null ? incident.getReporter().getUsername() : null)
                .assigneeUsername(incident.getAssignee() != null ? incident.getAssignee().getUsername() : null)
                .teamName(incident.getTeam() != null ? incident.getTeam().getName() : null)
                .teamId(incident.getTeam() != null ? incident.getTeam().getId() : null)
                .triageDeadline(incident.getTriageDeadline())
                .resolutionDeadline(incident.getResolutionDeadline())
                .createdAt(incident.getCreatedAt())
                .updatedAt(incident.getUpdatedAt())
                .build();
    }
}
