package com.voting.models;

import java.io.Serializable;

public class Candidate implements Serializable {
    private String id;
    private String name;
    private String party;
    private int voteCount;

    public Candidate(String id, String name, String party) {
        this.id = id;
        this.name = name;
        this.party = party;
        this.voteCount = 0;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getParty() { return party; }
    public int getVoteCount() { return voteCount; }
    
    public void incrementVoteCount() { this.voteCount++; }
    public void setVoteCount(int voteCount) { this.voteCount = voteCount; }
}
