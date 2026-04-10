package com.voting.services;

import com.voting.models.Admin;
import com.voting.models.Candidate;
import com.voting.models.User;
import com.voting.models.Voter;
import com.voting.utils.Logger;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class DatabaseService {
    private static final String URL = "jdbc:postgresql://aws-1-ap-northeast-1.pooler.supabase.com:6543/postgres";
    private static final String USER = "postgres.ifnifarcnmeleajhnslg";
    private static final String PASS = "numwax-xubvuv-vifgI3";

    public DatabaseService() {
        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("Connecting to Cloud Database...");
            initializeDatabase();
            System.out.println("Connected to Cloud Database successfully!");
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC Driver not found in classpath.");
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    private void initializeDatabase() {
        String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                "id VARCHAR(50) PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "password VARCHAR(100) NOT NULL, " +
                "role VARCHAR(20) NOT NULL, " +
                "has_voted BOOLEAN DEFAULT FALSE" +
                ");";

        String createCandidatesTable = "CREATE TABLE IF NOT EXISTS candidates (" +
                "id VARCHAR(50) PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "party VARCHAR(100) NOT NULL, " +
                "vote_count INT DEFAULT 0" +
                ");";

        String createSettingsTable = "CREATE TABLE IF NOT EXISTS settings (" +
                "setting_key VARCHAR(50) PRIMARY KEY, " +
                "setting_value VARCHAR(50) NOT NULL" +
                ");";
                
        String insertDefaultSetting = "INSERT INTO settings (setting_key, setting_value) VALUES ('election_active', 'false') ON CONFLICT DO NOTHING;";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createCandidatesTable);
            stmt.execute(createSettingsTable);
            stmt.execute(insertDefaultSetting);
        } catch (SQLException e) {
            Logger.log("DB ERROR (Init): " + e.getMessage());
            System.err.println("Database initialization failed: " + e.getMessage());
        }
    }

    public boolean isElectionActive() {
        String query = "SELECT setting_value FROM settings WHERE setting_key = 'election_active'";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return Boolean.parseBoolean(rs.getString("setting_value"));
            }
        } catch (SQLException e) {
            Logger.log("DB ERROR (isElectionActive): " + e.getMessage());
        }
        return false;
    }

    public void setElectionActive(boolean active) {
        String query = "UPDATE settings SET setting_value = ? WHERE setting_key = 'election_active'";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, String.valueOf(active));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            Logger.log("DB ERROR (setElectionActive): " + e.getMessage());
        }
    }

    public User getUserById(String id) {
        String query = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String role = rs.getString("role");
                String name = rs.getString("name");
                String pass = rs.getString("password");
                if ("ADMIN".equals(role)) {
                    return new Admin(id, name, pass);
                } else {
                    Voter v = new Voter(id, name, pass);
                    v.setHasVoted(rs.getBoolean("has_voted"));
                    return v;
                }
            }
        } catch (SQLException e) {
            Logger.log("DB ERROR (getUserById): " + e.getMessage());
        }
        return null;
    }

    public boolean insertUser(User user) {
        String query = "INSERT INTO users (id, name, password, role, has_voted) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, user.getId());
            pstmt.setString(2, user.getName());
            pstmt.setString(3, user.getPassword());
            pstmt.setString(4, user.getRole());
            pstmt.setBoolean(5, user instanceof Voter && ((Voter) user).hasVoted());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            Logger.log("DB ERROR (insertUser): " + e.getMessage());
            return false;
        }
    }

    public void updateVoterStatus(String id, boolean hasVoted) {
        String query = "UPDATE users SET has_voted = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setBoolean(1, hasVoted);
            pstmt.setString(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            Logger.log("DB ERROR (updateVoterStatus): " + e.getMessage());
        }
    }

    public List<Candidate> getAllCandidates() {
        List<Candidate> list = new ArrayList<>();
        String query = "SELECT * FROM candidates ORDER BY vote_count DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Candidate c = new Candidate(rs.getString("id"), rs.getString("name"), rs.getString("party"));
                c.setVoteCount(rs.getInt("vote_count"));
                list.add(c);
            }
        } catch (SQLException e) {
            Logger.log("DB ERROR (getAllCandidates): " + e.getMessage());
        }
        return list;
    }

    public boolean insertCandidate(Candidate c) {
        String query = "INSERT INTO candidates (id, name, party, vote_count) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, c.getId());
            pstmt.setString(2, c.getName());
            pstmt.setString(3, c.getParty());
            pstmt.setInt(4, c.getVoteCount());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            Logger.log("DB ERROR (insertCandidate): " + e.getMessage());
            return false;
        }
    }

    public boolean deleteCandidate(String id) {
        String query = "DELETE FROM candidates WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, id);
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            Logger.log("DB ERROR (deleteCandidate): " + e.getMessage());
            return false;
        }
    }

    public boolean incrementCandidateVote(String id) {
        String query = "UPDATE candidates SET vote_count = vote_count + 1 WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, id);
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            Logger.log("DB ERROR (incrementCandidateVote): " + e.getMessage());
            return false;
        }
    }

    public int getTotalVotes() {
        String query = "SELECT SUM(vote_count) FROM candidates";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            Logger.log("DB ERROR (getTotalVotes): " + e.getMessage());
        }
        return 0;
    }
}
