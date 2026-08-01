package com.team5.reflextrainer;

public class LeaderboardEntry {
    private String displayName;
    private int bestReactionMs;
    private int avatarId;   // snapshot of the avatar at the time this score was submitted

    public LeaderboardEntry() { }   // required by Firestore

    public LeaderboardEntry(String displayName, int bestReactionMs) {
        this.displayName = displayName;
        this.bestReactionMs = bestReactionMs;
    }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public int getBestReactionMs() { return bestReactionMs; }
    public void setBestReactionMs(int bestReactionMs) { this.bestReactionMs = bestReactionMs; }

    public int getAvatarId() { return avatarId; }
    public void setAvatarId(int avatarId) { this.avatarId = avatarId; }
}