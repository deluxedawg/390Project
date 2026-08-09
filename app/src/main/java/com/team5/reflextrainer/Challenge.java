package com.team5.reflextrainer;

public class Challenge {
    private String challengeId;
    private String fromUid, fromUsername;
    private String toUid, toUsername;
    private String difficulty;
    private int timeoutMs;
    private int rounds;
    private int fromScore;      // challenger's avg ms
    private int toScore;        // opponent's avg ms (0 until played)
    private String status;      // "pending" | "completed"
    private String winnerUid;   // set when completed ("" = tie)
    private long timestamp;

    public Challenge() { }      // required by Firestore

    public Challenge(String challengeId, String fromUid, String fromUsername,
                     String toUid, String toUsername, String difficulty,
                     int timeoutMs, int rounds, int fromScore, long timestamp) {
        this.challengeId = challengeId;
        this.fromUid = fromUid;
        this.fromUsername = fromUsername;
        this.toUid = toUid;
        this.toUsername = toUsername;
        this.difficulty = difficulty;
        this.timeoutMs = timeoutMs;
        this.rounds = rounds;
        this.fromScore = fromScore;
        this.toScore = 0;
        this.status = "pending";
        this.winnerUid = "";
        this.timestamp = timestamp;
    }

    public String getChallengeId() { return challengeId; }
    public void setChallengeId(String v) { this.challengeId = v; }
    public String getFromUid() { return fromUid; }
    public void setFromUid(String v) { this.fromUid = v; }
    public String getFromUsername() { return fromUsername; }
    public void setFromUsername(String v) { this.fromUsername = v; }
    public String getToUid() { return toUid; }
    public void setToUid(String v) { this.toUid = v; }
    public String getToUsername() { return toUsername; }
    public void setToUsername(String v) { this.toUsername = v; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String v) { this.difficulty = v; }
    public int getTimeoutMs() { return timeoutMs; }
    public void setTimeoutMs(int v) { this.timeoutMs = v; }
    public int getRounds() { return rounds; }
    public void setRounds(int v) { this.rounds = v; }
    public int getFromScore() { return fromScore; }
    public void setFromScore(int v) { this.fromScore = v; }
    public int getToScore() { return toScore; }
    public void setToScore(int v) { this.toScore = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public String getWinnerUid() { return winnerUid; }
    public void setWinnerUid(String v) { this.winnerUid = v; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long v) { this.timestamp = v; }
}
