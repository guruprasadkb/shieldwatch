package com.shieldwatch.repository;

import com.shieldwatch.model.Incident;
import com.shieldwatch.model.User;
import com.shieldwatch.model.enums.Severity;
import com.shieldwatch.model.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, String> {

    Page<Incident> findBySeverity(Severity severity, Pageable pageable);

    Page<Incident> findByStatus(Status status, Pageable pageable);

    Page<Incident> findBySeverityAndStatus(Severity severity, Status status, Pageable pageable);

    @Query("SELECT i FROM Incident i WHERE " +
           "(i.triageDeadline IS NOT NULL AND i.triageDeadline < :now AND i.status = 'OPEN') OR " +
           "(i.resolutionDeadline IS NOT NULL AND i.resolutionDeadline < :now AND i.status NOT IN ('RESOLVED', 'CLOSED', 'CANCELLED'))")
    Page<Incident> findBreachedSla(@Param("now") LocalDateTime now, Pageable pageable);

    long countByAssigneeAndStatusNotIn(User assignee, Collection<Status> statuses);

    long countBySeverity(Severity severity);

    long countByStatus(Status status);

    @Query("SELECT i.severity, COUNT(i) FROM Incident i GROUP BY i.severity")
    List<Object[]> countGroupBySeverity();

    @Query("SELECT i.status, COUNT(i) FROM Incident i GROUP BY i.status")
    List<Object[]> countGroupByStatus();
}
