package com.voting.controllers;

import com.voting.models.Candidate;
import com.voting.models.User;
import com.voting.services.VotingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Allow frontend requests
public class ApiController {

    @Autowired
    private VotingService votingService;

    @PostConstruct
    public void init() {
        votingService.init();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        User user = votingService.login(payload.get("id"), payload.get("password"));
        if (user != null) {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "id", user.getId(),
                "name", user.getName(),
                "role", user.getRole()
            ));
        }
        return ResponseEntity.status(401).body(Map.of("success", false, "message", "Invalid credentials"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> payload) {
        boolean success = votingService.registerVoter(payload.get("id"), payload.get("name"), payload.get("password"));
        if (success) {
            return ResponseEntity.ok(Map.of("success", true));
        }
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User ID already exists"));
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<?> getUser(@PathVariable String id) {
        User user = votingService.getUser(id);
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/candidates")
    public ResponseEntity<List<Candidate>> getCandidates() {
        return ResponseEntity.ok(votingService.getCandidates());
    }

    @GetMapping("/election/status")
    public ResponseEntity<?> getElectionStatus() {
        return ResponseEntity.ok(Map.of(
            "active", votingService.isElectionActive(),
            "totalVotes", votingService.getTotalVotesCast()
        ));
    }

    @PostMapping("/vote")
    public ResponseEntity<?> castVote(@RequestBody Map<String, String> payload) {
        String voterId = payload.get("voterId");
        String candidateId = payload.get("candidateId");
        boolean success = votingService.castVote(voterId, candidateId);
        if (success) {
            return ResponseEntity.ok(Map.of("success", true));
        }
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Voting failed. Election may be closed, or you already voted."));
    }

    @PostMapping("/admin/election/toggle")
    public ResponseEntity<?> toggleElection(@RequestBody Map<String, Object> payload) {
        String adminId = (String) payload.get("adminId");
        boolean active = (Boolean) payload.get("active");
        votingService.setElectionActive(adminId, active);
        return ResponseEntity.ok(Map.of("success", true, "active", votingService.isElectionActive()));
    }

    @PostMapping("/admin/candidate")
    public ResponseEntity<?> addCandidate(@RequestBody Map<String, String> payload) {
        String adminId = payload.get("adminId");
        boolean success = votingService.addCandidate(adminId, payload.get("id"), payload.get("name"), payload.get("party"));
        if (success) {
            return ResponseEntity.ok(Map.of("success", true));
        }
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Failed to add candidate."));
    }

    @DeleteMapping("/admin/candidate/{id}")
    public ResponseEntity<?> deleteCandidate(@RequestParam String adminId, @PathVariable String id) {
        boolean success = votingService.deleteCandidate(adminId, id);
        if (success) {
            return ResponseEntity.ok(Map.of("success", true));
        }
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Cannot delete candidate with votes."));
    }
}
