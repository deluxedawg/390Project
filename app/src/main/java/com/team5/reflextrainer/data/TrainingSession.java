package com.team5.reflextrainer.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "training_sessions")
public class TrainingSession {
    @PrimaryKey(autoGenerate = true)
    private long sessionId;

    private String userId;
    private long durationMs;
    private String status;        // "Completed" or "Terminated"
    private long timestamp;

    public TrainingSession(String userId, long durationMs, String status, long timestamp) {
        this.userId = userId;
        this.durationMs = durationMs;
        this.status = status;
        this.timestamp = timestamp;
    }

    public long getSessionId() { return sessionId; }
    public void setSessionId(long sessionId) { this.sessionId = sessionId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}