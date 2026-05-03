package com.shieldwatch.controller;

import com.shieldwatch.repository.IncidentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final IncidentRepository incidentRepository;

    public DashboardController(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        Map<String, Object> metrics = new LinkedHashMap<>();

        metrics.put("totalIncidents", incidentRepository.count());

        Map<String, Long> bySeverity = incidentRepository.countGroupBySeverity().stream()
                .collect(Collectors.toMap(
                        row -> row[0].toString(),
                        row -> (Long) row[1]
                ));
        metrics.put("bySeverity", bySeverity);

        Map<String, Long> byStatus = incidentRepository.countGroupByStatus().stream()
                .collect(Collectors.toMap(
                        row -> row[0].toString(),
                        row -> (Long) row[1]
                ));
        metrics.put("byStatus", byStatus);

        return ResponseEntity.ok(metrics);
    }
}
