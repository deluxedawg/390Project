package com.team5.reflextrainer.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "training_sessions")
public class TrainingSession {
    @PrimaryKey(autoGenerate = true)
    private long sessionId;

    private String userId;
    private int avgReactionMs;
    private int bestReactionMs;
    private int totalRounds;
    private int correctRounds;
    private String difficulty;
    private long timestamp;

    public TrainingSession(String userId, int avgReactionMs, int bestReactionMs,
                           int totalRounds, int correctRounds, String difficulty, long timestamp) {
        this.userId = userId;
        this.avgReactionMs = avgReactionMs;
        this.bestReactionMs = bestReactionMs;
        this.totalRounds = totalRounds;
        this.correctRounds = correctRounds;
        this.difficulty = difficulty;
        this.timestamp = timestamp;
    }

    public long getSessionId() { return sessionId; }
    public void setSessionId(long v) { this.sessionId = v; }
    public String getUserId() { return userId; }
    public void setUserId(String v) { this.userId = v; }
    public int getAvgReactionMs() { return avgReactionMs; }
    public void setAvgReactionMs(int v) { this.avgReactionMs = v; }
    public int getBestReactionMs() { return bestReactionMs; }
    public void setBestReactionMs(int v) { this.bestReactionMs = v; }
    public int getTotalRounds() { return totalRounds; }
    public void setTotalRounds(int v) { this.totalRounds = v; }
    public int getCorrectRounds() { return correctRounds; }
    public void setCorrectRounds(int v) { this.correctRounds = v; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String v) { this.difficulty = v; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long v) { this.timestamp = v; }
}
