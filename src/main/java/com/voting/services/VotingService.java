package com.voting.services;

import com.voting.models.Admin;
import com.voting.models.Candidate;
import com.voting.models.User;
import com.voting.models.Voter;
import com.voting.utils.Logger;
import com.voting.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VotingService {
    
    @Autowired
    private DatabaseService dbService;

    public VotingService() {}

    public void init() {
        // Create default admin if no users exist
        User admin = dbService.getUserById("admin");
        if (admin == null) {
            String defaultAdminPassword = "admin123";
            dbService.insertUser(new Admin("admin", "Admin", SecurityUtil.hashPassword(defaultAdminPassword)));
            Logger.log("SYSTEM: Initialized default admin account in Cloud DB.");
        }
    }
    
    public boolean isElectionActive() {
        return dbService.isElectionActive();
    }

    public void setElectionActive(String adminId, boolean active) {
        User user = dbService.getUserById(adminId);
        if (user instanceof Admin) {
            dbService.setElectionActive(active);
            Logger.log("ELECTION STATUS CHANGED: Active=" + active + " by Admin " + adminId);
        }
    }

    public User login(String id, String password) {
        String hashedPassword = SecurityUtil.hashPassword(password);
        User user = dbService.getUserById(id);
        
        if (user != null && user.getPassword().equals(hashedPassword)) {
            Logger.log("LOGIN: User " + id + " logged in successfully.");
            return user;
        }
        
        Logger.log("LOGIN FAILED: Attempted login for user " + id);
        return null;
    }

    public User getUser(String id) {
        return dbService.getUserById(id);
    }

    public boolean registerVoter(String id, String name, String password) {
        if (dbService.getUserById(id) != null) {
            Logger.log("REGISTRATION FAILED: User ID " + id + " already exists.");
            return false;
        }
        boolean success = dbService.insertUser(new Voter(id, name, SecurityUtil.hashPassword(password)));
        if (success) {
            Logger.log("REGISTRATION: New voter registered with ID " + id);
        }
        return success;
    }

    public boolean addCandidate(String adminId, String id, String name, String party) {
        User admin = dbService.getUserById(adminId);
        if (!(admin instanceof Admin)) return false;

        boolean success = dbService.insertCandidate(new Candidate(id, name, party));
        if (success) {
            Logger.log("ADD CANDIDATE: Admin " + adminId + " added candidate " + name + " (" + party + ")");
        }
        return success;
    }

    public boolean deleteCandidate(String adminId, String id) {
        User admin = dbService.getUserById(adminId);
        if (!(admin instanceof Admin)) return false;

        List<Candidate> all = dbService.getAllCandidates();
        for(Candidate c : all) {
            if(c.getId().equals(id) && c.getVoteCount() > 0) {
                 Logger.log("DELETE CANDIDATE FAILED: Candidate " + id + " already has votes.");
                 return false;
            }
        }

        boolean success = dbService.deleteCandidate(id);
        if (success) {
            Logger.log("DELETE CANDIDATE: Admin " + adminId + " deleted candidate " + id);
        }
        return success;
    }

    public List<Candidate> getCandidates() {
        return dbService.getAllCandidates();
    }

    public int getTotalVotesCast() {
        return dbService.getTotalVotes();
    }

    public boolean castVote(String voterId, String candidateId) {
        User currentUser = dbService.getUserById(voterId);
        if (!(currentUser instanceof Voter)) return false;
        if (!isElectionActive()) {
            Logger.log("VOTE BLOCKED: User " + voterId + " attempted to vote while election inactive.");
            return false;
        }
        
        Voter voter = (Voter) currentUser;
        if (voter.hasVoted()) {
            Logger.log("VOTE BLOCKED: User " + voterId + " attempted to vote again.");
            return false; // Already voted
        }

        boolean voteSuccess = dbService.incrementCandidateVote(candidateId);
        if (voteSuccess) {
            dbService.updateVoterStatus(voter.getId(), true);
            Logger.log("VOTE CAST: User " + voter.getId() + " cast a vote for " + candidateId);
            return true;
        }

        Logger.log("VOTE FAILED: User " + voter.getId() + " tried to vote for invalid candidate ID " + candidateId);
        return false; 
    }
}
