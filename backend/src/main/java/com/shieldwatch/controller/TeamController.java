package com.shieldwatch.controller;

import com.shieldwatch.model.Team;
import com.shieldwatch.model.User;
import com.shieldwatch.repository.TeamRepository;
import com.shieldwatch.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    public TeamController(TeamRepository teamRepository, UserRepository userRepository) {
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listTeams() {
        List<Map<String, Object>> teams = teamRepository.findAll().stream()
                .map(t -> Map.<String, Object>of(
                        "id", t.getId(),
                        "name", t.getName(),
                        "description", t.getDescription() != null ? t.getDescription() : ""
                ))
                .toList();
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<Map<String, Object>>> getTeamMembers(@PathVariable String id) {
        List<User> members = userRepository.findByTeamId(id);
        List<Map<String, Object>> result = members.stream()
                .map(u -> Map.<String, Object>of(
                        "id", u.getId(),
                        "username", u.getUsername(),
                        "displayName", u.getDisplayName(),
                        "role", u.getRole().name()
                ))
                .toList();
        return ResponseEntity.ok(result);
    }
}
