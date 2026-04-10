package com.voting.models;

public class Voter extends User {
    private boolean hasVoted;

    public Voter(String id, String name, String password) {
        super(id, name, password);
        this.hasVoted = false;
    }

    public boolean hasVoted() { return hasVoted; }
    public void setHasVoted(boolean hasVoted) { this.hasVoted = hasVoted; }

    @Override
    public String getRole() { return "VOTER"; }
}
