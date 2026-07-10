package com.team5.reflextrainer.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "training_sessions")
public class TrainingSession {
    @PrimaryKey(autoGenerate = true)
    private long sessionId;

    private String userId;
    private int reactionTimeMs;
    private String trainingMode;
    private String difficulty;
    private long timestamp;

    public TrainingSession(String userId, int reactionTimeMs, String trainingMode, String difficulty, long timestamp) {
        this.userId = userId;
        this.reactionTimeMs = reactionTimeMs;
        this.trainingMode = trainingMode;
        this.difficulty = difficulty;
        this.timestamp = timestamp;
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getReactionTimeMs() {
        return reactionTimeMs;
    }

    public void setReactionTimeMs(int reactionTimeMs) {
        this.reactionTimeMs = reactionTimeMs;
    }

    public String getTrainingMode() {
        return trainingMode;
    }

    public void setTrainingMode(String trainingMode) {
        this.trainingMode = trainingMode;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
