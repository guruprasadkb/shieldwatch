package com.shieldwatch.config;

import com.shieldwatch.model.*;
import com.shieldwatch.model.enums.*;
import com.shieldwatch.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner loadData(TeamRepository teamRepo, UserRepository userRepo,
                               IncidentRepository incidentRepo, AuditLogRepository auditRepo,
                               PasswordEncoder passwordEncoder) {
        return args -> {
            if (teamRepo.count() > 0) return;

            String encodedPassword = passwordEncoder.encode("password123");

            // Teams
            Team alpha = teamRepo.save(Team.builder().id("team-alpha").name("Alpha").description("Threat Detection").build());
            Team bravo = teamRepo.save(Team.builder().id("team-bravo").name("Bravo").description("Incident Response").build());
            Team charlie = teamRepo.save(Team.builder().id("team-charlie").name("Charlie").description("Forensics").build());

            // Users
            User analyst1 = userRepo.save(User.builder().id("user-analyst1").username("analyst1").password(encodedPassword).displayName("Alice Chen").role(Role.ANALYST).team(alpha).build());
            User analyst2 = userRepo.save(User.builder().id("user-analyst2").username("analyst2").password(encodedPassword).displayName("Bob Patel").role(Role.ANALYST).team(bravo).build());
            User leadAlpha = userRepo.save(User.builder().id("user-lead-alpha").username("lead_alpha").password(encodedPassword).displayName("Carol Davis").role(Role.LEAD).team(alpha).build());
            User leadBravo = userRepo.save(User.builder().id("user-lead-bravo").username("lead_bravo").password(encodedPassword).displayName("Dan Kim").role(Role.LEAD).team(bravo).build());
            User admin1 = userRepo.save(User.builder().id("user-admin1").username("admin1").password(encodedPassword).displayName("Eve Wilson").role(Role.ADMIN).team(alpha).build());

            LocalDateTime now = LocalDateTime.now();

            // CRITICAL incidents (5)
            incidentRepo.save(Incident.builder().id("inc-001").title("Unauthorized access to production database").description("Multiple failed login attempts followed by successful authentication from unknown IP 203.0.113.42").severity(Severity.CRITICAL).status(Status.OPEN).reporter(analyst1).assignee(leadAlpha).team(alpha).triageDeadline(now.minusHours(2).plusMinutes(15)).resolutionDeadline(now.minusHours(2).plusHours(4)).createdAt(now.minusHours(2)).build());
            incidentRepo.save(Incident.builder().id("inc-002").title("Ransomware detected on file server").description("CryptoLocker variant detected by endpoint protection on FS-PROD-03").severity(Severity.CRITICAL).status(Status.TRIAGED).reporter(analyst1).assignee(leadAlpha).team(alpha).triageDeadline(now.minusHours(1).plusMinutes(15)).resolutionDeadline(now.minusHours(1).plusHours(4)).createdAt(now.minusHours(1)).build());
            incidentRepo.save(Incident.builder().id("inc-003").title("DDoS attack on public API gateway").description("Traffic spike to 50x normal levels from botnet").severity(Severity.CRITICAL).status(Status.INVESTIGATING).reporter(analyst2).assignee(leadBravo).team(bravo).triageDeadline(now.minusHours(3).plusMinutes(15)).resolutionDeadline(now.minusHours(3).plusHours(4)).createdAt(now.minusHours(3)).build());
            incidentRepo.save(Incident.builder().id("inc-004").title("Data exfiltration attempt via DNS tunneling").description("Anomalous DNS query patterns detected from internal host 10.0.5.22").severity(Severity.CRITICAL).status(Status.RESOLVED).reporter(analyst1).assignee(analyst1).team(alpha).triageDeadline(now.minusHours(12).plusMinutes(15)).resolutionDeadline(now.minusHours(12).plusHours(4)).createdAt(now.minusHours(12)).build());
            incidentRepo.save(Incident.builder().id("inc-005").title("Zero-day exploit in web application firewall").description("WAF bypass using novel payload encoding technique").severity(Severity.CRITICAL).status(Status.CLOSED).reporter(analyst2).assignee(leadBravo).team(bravo).triageDeadline(now.minusHours(24).plusMinutes(15)).resolutionDeadline(now.minusHours(24).plusHours(4)).createdAt(now.minusHours(24)).build());

            // HIGH incidents (7)
            incidentRepo.save(Incident.builder().id("inc-006").title("Brute force attack on SSH bastion host").description("Over 10,000 failed SSH login attempts in the last hour").severity(Severity.HIGH).status(Status.OPEN).reporter(analyst1).team(alpha).triageDeadline(now.minusMinutes(30).plusHours(1)).resolutionDeadline(now.minusMinutes(30).plusHours(4)).createdAt(now.minusMinutes(30)).build());
            incidentRepo.save(Incident.builder().id("inc-007").title("Suspicious lateral movement in network").description("Host 10.0.3.15 scanning multiple internal subnets").severity(Severity.HIGH).status(Status.TRIAGED).reporter(analyst2).assignee(analyst2).team(bravo).triageDeadline(now.minusHours(4).plusHours(1)).resolutionDeadline(now.minusHours(4).plusHours(4)).createdAt(now.minusHours(4)).build());
            incidentRepo.save(Incident.builder().id("inc-008").title("Phishing campaign targeting engineering team").description("Credential harvesting emails mimicking internal IT communications").severity(Severity.HIGH).status(Status.INVESTIGATING).reporter(analyst1).assignee(analyst1).team(alpha).triageDeadline(now.minusHours(6).plusHours(1)).resolutionDeadline(now.minusHours(6).plusHours(4)).createdAt(now.minusHours(6)).build());
            incidentRepo.save(Incident.builder().id("inc-009").title("Malware beacon detected in web server logs").description("Regular HTTP POST requests to known C2 domain every 60 seconds").severity(Severity.HIGH).status(Status.RESOLVED).reporter(analyst2).assignee(leadBravo).team(bravo).triageDeadline(now.minusHours(20).plusHours(1)).resolutionDeadline(now.minusHours(20).plusHours(4)).createdAt(now.minusHours(20)).build());
            incidentRepo.save(Incident.builder().id("inc-010").title("Privilege escalation on container host").description("Non-root container process gained host-level access via kernel exploit").severity(Severity.HIGH).status(Status.CLOSED).reporter(analyst1).assignee(leadAlpha).team(alpha).triageDeadline(now.minusHours(36).plusHours(1)).resolutionDeadline(now.minusHours(36).plusHours(4)).createdAt(now.minusHours(36)).build());
            incidentRepo.save(Incident.builder().id("inc-011").title("API key exposed in public repository").description("Production API key for payment service found in GitHub commit").severity(Severity.HIGH).status(Status.OPEN).reporter(analyst2).team(bravo).triageDeadline(now.minusMinutes(45).plusHours(1)).resolutionDeadline(now.minusMinutes(45).plusHours(4)).createdAt(now.minusMinutes(45)).build());
            incidentRepo.save(Incident.builder().id("inc-012").title("Unauthorized VPN connection from foreign IP").description("VPN tunnel established from IP geolocated to restricted country").severity(Severity.HIGH).status(Status.REOPENED).reporter(analyst1).assignee(analyst1).team(alpha).triageDeadline(now.minusHours(48).plusHours(1)).resolutionDeadline(now.minusHours(48).plusHours(4)).createdAt(now.minusHours(48)).build());

            // MEDIUM incidents (8)
            incidentRepo.save(Incident.builder().id("inc-013").title("Outdated SSL certificate on internal service").description("Certificate for internal.corp expires in 7 days").severity(Severity.MEDIUM).status(Status.OPEN).reporter(analyst1).team(alpha).triageDeadline(now.minusHours(1).plusHours(4)).resolutionDeadline(now.minusHours(1).plusHours(72)).createdAt(now.minusHours(1)).build());
            incidentRepo.save(Incident.builder().id("inc-014").title("Failed security audit on staging environment").description("Automated scan found 3 medium-severity vulnerabilities").severity(Severity.MEDIUM).status(Status.TRIAGED).reporter(analyst2).assignee(analyst2).team(bravo).triageDeadline(now.minusHours(8).plusHours(4)).resolutionDeadline(now.minusHours(8).plusHours(72)).createdAt(now.minusHours(8)).build());
            incidentRepo.save(Incident.builder().id("inc-015").title("Excessive failed login attempts on admin panel").description("150 failed login attempts on admin portal in 24 hours").severity(Severity.MEDIUM).status(Status.INVESTIGATING).reporter(analyst1).assignee(analyst1).team(alpha).triageDeadline(now.minusHours(10).plusHours(4)).resolutionDeadline(now.minusHours(10).plusHours(72)).createdAt(now.minusHours(10)).build());
            incidentRepo.save(Incident.builder().id("inc-016").title("Unpatched CVE on database server").description("CVE-2024-1234 affects PostgreSQL version running on DB-PROD-01").severity(Severity.MEDIUM).status(Status.RESOLVED).reporter(analyst2).assignee(leadBravo).team(bravo).triageDeadline(now.minusHours(24).plusHours(4)).resolutionDeadline(now.minusHours(24).plusHours(72)).createdAt(now.minusHours(24)).build());
            incidentRepo.save(Incident.builder().id("inc-017").title("Misconfigured firewall rule allowing broad access").description("Rule FW-2847 permits 0.0.0.0/0 inbound on port 8080").severity(Severity.MEDIUM).status(Status.CLOSED).reporter(analyst1).assignee(leadAlpha).team(alpha).triageDeadline(now.minusHours(40).plusHours(4)).resolutionDeadline(now.minusHours(40).plusHours(72)).createdAt(now.minusHours(40)).build());
            incidentRepo.save(Incident.builder().id("inc-018").title("Anomalous outbound data transfer").description("Host 10.0.2.8 transferred 2GB to external IP overnight").severity(Severity.MEDIUM).status(Status.OPEN).reporter(analyst2).team(bravo).triageDeadline(now.minusHours(3).plusHours(4)).resolutionDeadline(now.minusHours(3).plusHours(72)).createdAt(now.minusHours(3)).build());
            incidentRepo.save(Incident.builder().id("inc-019").title("Service account password not rotated").description("SVC-DEPLOY account password has not been rotated in 180 days").severity(Severity.MEDIUM).status(Status.TRIAGED).reporter(analyst1).assignee(analyst1).team(alpha).triageDeadline(now.minusHours(12).plusHours(4)).resolutionDeadline(now.minusHours(12).plusHours(72)).createdAt(now.minusHours(12)).build());
            incidentRepo.save(Incident.builder().id("inc-020").title("Shadow IT cloud storage detected").description("Employees uploading sensitive documents to unauthorized cloud service").severity(Severity.MEDIUM).status(Status.INVESTIGATING).reporter(analyst2).assignee(analyst2).team(bravo).triageDeadline(now.minusHours(15).plusHours(4)).resolutionDeadline(now.minusHours(15).plusHours(72)).createdAt(now.minusHours(15)).build());

            // LOW incidents (5)
            incidentRepo.save(Incident.builder().id("inc-021").title("Security awareness training overdue for 3 employees").description("Annual security training completion rate at 94%").severity(Severity.LOW).status(Status.OPEN).reporter(analyst1).team(alpha).createdAt(now.minusHours(2)).build());
            incidentRepo.save(Incident.builder().id("inc-022").title("Guest WiFi network not segmented from IoT devices").description("Network scan shows guest and IoT VLANs can communicate").severity(Severity.LOW).status(Status.TRIAGED).reporter(analyst2).assignee(analyst2).team(bravo).createdAt(now.minusHours(20)).build());
            incidentRepo.save(Incident.builder().id("inc-023").title("Log retention policy not enforced on dev servers").description("Development servers retaining logs beyond 30-day policy").severity(Severity.LOW).status(Status.RESOLVED).reporter(analyst1).assignee(analyst1).team(alpha).createdAt(now.minusHours(36)).build());
            incidentRepo.save(Incident.builder().id("inc-024").title("Unused service accounts in Active Directory").description("12 service accounts with no login in 90 days").severity(Severity.LOW).status(Status.CLOSED).reporter(analyst2).assignee(leadBravo).team(bravo).createdAt(now.minusHours(48)).build());
            incidentRepo.save(Incident.builder().id("inc-025").title("Documentation outdated for incident response plan").description("IR playbook references deprecated tools and old contact list").severity(Severity.LOW).status(Status.OPEN).reporter(analyst1).team(alpha).createdAt(now.minusHours(5)).build());

            // Audit logs
            auditRepo.saveAll(List.of(
                AuditLog.builder().incidentId("inc-001").performedBy("analyst1").action("CREATED").newValue("OPEN").timestamp(now.minusHours(2)).build(),
                AuditLog.builder().incidentId("inc-002").performedBy("analyst1").action("CREATED").newValue("OPEN").timestamp(now.minusHours(1)).build(),
                AuditLog.builder().incidentId("inc-002").performedBy("lead_alpha").action("STATUS_CHANGE").oldValue("OPEN").newValue("TRIAGED").timestamp(now.minusMinutes(50)).build(),
                AuditLog.builder().incidentId("inc-003").performedBy("analyst2").action("CREATED").newValue("OPEN").timestamp(now.minusHours(3)).build(),
                AuditLog.builder().incidentId("inc-003").performedBy("lead_bravo").action("STATUS_CHANGE").oldValue("OPEN").newValue("TRIAGED").timestamp(now.minusHours(2).minusMinutes(30)).build(),
                AuditLog.builder().incidentId("inc-003").performedBy("lead_bravo").action("STATUS_CHANGE").oldValue("TRIAGED").newValue("INVESTIGATING").timestamp(now.minusHours(2)).build(),
                AuditLog.builder().incidentId("inc-004").performedBy("analyst1").action("CREATED").newValue("OPEN").timestamp(now.minusHours(12)).build(),
                AuditLog.builder().incidentId("inc-004").performedBy("lead_alpha").action("STATUS_CHANGE").oldValue("OPEN").newValue("TRIAGED").timestamp(now.minusHours(11)).build(),
                AuditLog.builder().incidentId("inc-004").performedBy("analyst1").action("STATUS_CHANGE").oldValue("TRIAGED").newValue("INVESTIGATING").timestamp(now.minusHours(10)).build(),
                AuditLog.builder().incidentId("inc-004").performedBy("analyst1").action("STATUS_CHANGE").oldValue("INVESTIGATING").newValue("RESOLVED").timestamp(now.minusHours(8)).build(),
                AuditLog.builder().incidentId("inc-005").performedBy("analyst2").action("CREATED").newValue("OPEN").timestamp(now.minusHours(24)).build(),
                AuditLog.builder().incidentId("inc-005").performedBy("admin1").action("STATUS_CHANGE").oldValue("RESOLVED").newValue("CLOSED").timestamp(now.minusHours(18)).build()
            ));
        };
    }
}
