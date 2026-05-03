package com.shieldwatch.controller;

import com.shieldwatch.dto.*;
import com.shieldwatch.model.AuditLog;
import com.shieldwatch.model.Incident;
import com.shieldwatch.model.enums.Severity;
import com.shieldwatch.model.enums.Status;
import com.shieldwatch.service.AuditService;
import com.shieldwatch.service.IncidentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService incidentService;
    private final AuditService auditService;

    public IncidentController(IncidentService incidentService, AuditService auditService) {
        this.incidentService = incidentService;
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listIncidents(
            @RequestParam(required = false) Severity severity,
            @RequestParam(required = false) Status status,
            @RequestParam(name = "sla_status", required = false) String slaStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        size = Math.min(size, 200);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Incident> incidents = incidentService.listIncidents(severity, status, slaStatus, pageable);

        Map<String, Object> response = Map.of(
                "content", incidents.getContent().stream().map(IncidentResponse::from).toList(),
                "page", incidents.getNumber(),
                "size", incidents.getSize(),
                "totalElements", incidents.getTotalElements(),
                "totalPages", incidents.getTotalPages()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncidentResponse> getIncident(@PathVariable String id) {
        Incident incident = incidentService.getIncident(id);
        return ResponseEntity.ok(IncidentResponse.from(incident));
    }

    @PostMapping
    public ResponseEntity<IncidentResponse> createIncident(
            @Valid @RequestBody CreateIncidentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Incident incident = incidentService.createIncident(
                request.getTitle(),
                request.getDescription(),
                request.getSeverity(),
                userDetails.getUsername(),
                request.getTeamId());

        return ResponseEntity.status(201).body(IncidentResponse.from(incident));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IncidentResponse> updateIncident(
            @PathVariable String id,
            @RequestBody UpdateIncidentRequest request) {

        Incident incident = incidentService.updateIncident(
                id, request.getTitle(), request.getDescription(), request.getSeverity());

        return ResponseEntity.ok(IncidentResponse.from(incident));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncident(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {

        incidentService.deleteIncident(id, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/transition")
    public ResponseEntity<IncidentResponse> transitionIncident(
            @PathVariable String id,
            @Valid @RequestBody TransitionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Incident incident = incidentService.transitionStatus(id, request.getStatus(), userDetails.getUsername());
        return ResponseEntity.ok(IncidentResponse.from(incident));
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<IncidentResponse> assignIncident(
            @PathVariable String id,
            @Valid @RequestBody AssignRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Incident incident = incidentService.assignIncident(id, request.getAssigneeId(), userDetails.getUsername());
        return ResponseEntity.ok(IncidentResponse.from(incident));
    }

    @GetMapping("/{id}/audit")
    public ResponseEntity<List<AuditLog>> getAuditTrail(@PathVariable String id) {
        // Verify incident exists
        incidentService.getIncident(id);
        return ResponseEntity.ok(auditService.getAuditTrail(id));
    }
}
