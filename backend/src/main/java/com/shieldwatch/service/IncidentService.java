package com.shieldwatch.service;

import com.shieldwatch.exception.BusinessException;
import com.shieldwatch.exception.ResourceNotFoundException;
import com.shieldwatch.model.Incident;
import com.shieldwatch.model.User;
import com.shieldwatch.model.enums.Role;
import com.shieldwatch.model.enums.Severity;
import com.shieldwatch.model.enums.Status;
import com.shieldwatch.repository.IncidentRepository;
import com.shieldwatch.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;
    private final EscalationService escalationService;
    private final AuditService auditService;
    private final NotificationService notificationService;

    private static final Set<Status> TERMINAL_STATUSES = Set.of(Status.RESOLVED, Status.CLOSED, Status.CANCELLED);

    private static final Map<Status, Set<Status>> ALLOWED_TRANSITIONS = Map.of(
            Status.OPEN, Set.of(Status.TRIAGED, Status.RESOLVED, Status.CANCELLED),
            Status.TRIAGED, Set.of(Status.INVESTIGATING, Status.CANCELLED),
            Status.INVESTIGATING, Set.of(Status.RESOLVED, Status.CANCELLED),
            Status.RESOLVED, Set.of(Status.CLOSED, Status.REOPENED),
            Status.REOPENED, Set.of(Status.INVESTIGATING, Status.CANCELLED),
            Status.CLOSED, Set.of(),
            Status.CANCELLED, Set.of()
    );

    public IncidentService(IncidentRepository incidentRepository,
                           UserRepository userRepository,
                           EscalationService escalationService,
                           AuditService auditService,
                           NotificationService notificationService) {
        this.incidentRepository = incidentRepository;
        this.userRepository = userRepository;
        this.escalationService = escalationService;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    @Transactional
    public Incident createIncident(String title, String description, Severity severity,
                                    String reporterUsername, String teamId) {
        User reporter = userRepository.findByUsername(reporterUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Incident incident = Incident.builder()
                .title(title)
                .description(description)
                .severity(severity)
                .status(Status.OPEN)
                .reporter(reporter)
                .createdAt(LocalDateTime.now())
                .build();

        if (teamId != null) {
            incident.setTeam(reporter.getTeam());
        } else {
            incident.setTeam(reporter.getTeam());
        }

        escalationService.calculateDeadlines(incident);

        // Auto-assign CRITICAL incidents to team lead
        if (severity == Severity.CRITICAL && incident.getTeam() != null) {
            incident.getTeam().getMembers().stream()
                    .filter(u -> u.getRole() == Role.LEAD)
                    .findFirst()
                    .ifPresent(lead -> {
                        incident.setAssignee(lead);
                        auditService.log(incident.getId(), "SYSTEM", "AUTO_ASSIGN",
                                null, lead.getUsername());
                    });
        }

        Incident saved = incidentRepository.save(incident);
        auditService.log(saved.getId(), reporterUsername, "CREATED", null, saved.getStatus().name());

        return saved;
    }

    public Incident getIncident(String id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found: " + id));
    }

    public Page<Incident> listIncidents(Severity severity, Status status, String slaStatus, Pageable pageable) {
        if ("breached".equalsIgnoreCase(slaStatus)) {
            return incidentRepository.findBreachedSla(LocalDateTime.now(), pageable);
        }
        if (severity != null && status != null) {
            return incidentRepository.findBySeverityAndStatus(severity, status, pageable);
        }
        if (severity != null) {
            return incidentRepository.findBySeverity(severity, pageable);
        }
        if (status != null) {
            return incidentRepository.findByStatus(status, pageable);
        }
        return incidentRepository.findAll(pageable);
    }

    @Transactional
    public Incident updateIncident(String id, String title, String description, Severity severity) {
        Incident incident = getIncident(id);

        if (title != null) incident.setTitle(title);
        if (description != null) incident.setDescription(description);
        if (severity != null) {
            incident.setSeverity(severity);
            escalationService.calculateDeadlines(incident);
        }

        return incidentRepository.save(incident);
    }

    @Transactional
    public void deleteIncident(String id, String performedBy) {
        Incident incident = getIncident(id);
        auditService.log(id, performedBy, "DELETED", incident.getStatus().name(), null);
        incidentRepository.delete(incident);
    }

    @Transactional
    public Incident transitionStatus(String id, Status newStatus, String username) {
        Incident incident = getIncident(id);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Status currentStatus = incident.getStatus();

        // Check if transition is allowed
        Set<Status> allowed = ALLOWED_TRANSITIONS.get(currentStatus);
        if (allowed == null || !allowed.contains(newStatus)) {
            throw new BusinessException(
                    String.format("Cannot transition from %s to %s", currentStatus, newStatus));
        }

        // Only ADMIN can cancel
        if (newStatus == Status.CANCELLED && user.getRole() != Role.ADMIN) {
            throw new BusinessException("Only ADMIN can cancel incidents");
        }

        // ANALYST can only transition their own incidents
        if (user.getRole() == Role.ANALYST) {
            if (incident.getAssignee() == null || !incident.getAssignee().getId().equals(user.getId())) {
                if (incident.getReporter() == null || !incident.getReporter().getId().equals(user.getId())) {
                    throw new BusinessException("Analysts can only transition their own incidents");
                }
            }
        }

        // LEAD can transition any incident in their team
        if (user.getRole() == Role.LEAD) {
            if (incident.getTeam() != null && user.getTeam() != null
                    && !incident.getTeam().getId().equals(user.getTeam().getId())) {
                throw new BusinessException("Leads can only transition incidents in their team");
            }
        }

        String oldStatus = currentStatus.name();
        incident.setStatus(newStatus);
        Incident saved = incidentRepository.save(incident);

        auditService.log(id, username, "STATUS_CHANGE", oldStatus, newStatus.name());
        notificationService.notifyStatusChange(id, oldStatus, newStatus.name());

        return saved;
    }

    @Transactional
    public Incident assignIncident(String id, String assigneeId, String requestorUsername) {
        Incident incident = getIncident(id);
        User requestor = userRepository.findByUsername(requestorUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Requestor not found"));
        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignee not found: " + assigneeId));

        // Only LEAD or ADMIN can reassign
        if (requestor.getRole() == Role.ANALYST) {
            throw new BusinessException("Analysts cannot reassign incidents");
        }

        // Cannot assign closed or cancelled incidents
        if (incident.getStatus() == Status.CLOSED || incident.getStatus() == Status.CANCELLED) {
            throw new BusinessException("Cannot assign a " + incident.getStatus() + " incident");
        }

        // Check team membership for LEAD
        if (requestor.getRole() == Role.LEAD) {
            // Verify requestor is on the same team as the incident
            if (incident.getTeam() != null && requestor.getTeam() != null
                    && !incident.getTeam().getId().equals(requestor.getTeam().getId())) {
                throw new BusinessException("Leads can only reassign incidents in their own team");
            }
        }

        // Check assignee active incident count
        long activeCount = incidentRepository.countByAssigneeAndStatusNotIn(assignee, TERMINAL_STATUSES);
        if (activeCount > 5) {
            throw new BusinessException("User already has too many active incidents");
        }

        String oldAssignee = incident.getAssignee() != null ? incident.getAssignee().getUsername() : null;
        incident.setAssignee(assignee);
        Incident saved = incidentRepository.save(incident);

        auditService.log(id, requestorUsername, "REASSIGNED", oldAssignee, assignee.getUsername());
        notificationService.notifyAssignment(id, assigneeId);

        return saved;
    }
}
