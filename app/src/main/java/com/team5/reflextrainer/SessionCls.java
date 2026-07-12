package com.team5.reflextrainer;

import java.util.Date;

public class SessionCls {

    private long durationMs;
    private String status;      // "Completed" or "Terminated"
    private Date sessionDate;

    public SessionCls() {
        this.sessionDate = new Date();
        this.status = "Completed";
        this.durationMs = 0L;
    }

    public SessionCls(long durationMs, String status, Date sessionDate) {
        this.durationMs = durationMs;
        this.status = status;
        this.sessionDate = sessionDate;
    }

    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getSessionDate() { return sessionDate; }
    public void setSessionDate(Date sessionDate) { this.sessionDate = sessionDate; }
}