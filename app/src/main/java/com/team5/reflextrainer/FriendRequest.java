package com.team5.reflextrainer;

public class FriendRequest {
    private String fromUid;
    private String fromUsername;
    private String toUid;
    private String toUsername;
    private String status;   // "pending" or "accepted"

    public FriendRequest() { }   // required by Firestore

    public FriendRequest(String fromUid, String fromUsername,
                         String toUid, String toUsername, String status) {
        this.fromUid = fromUid;
        this.fromUsername = fromUsername;
        this.toUid = toUid;
        this.toUsername = toUsername;
        this.status = status;
    }

    public String getFromUid() { return fromUid; }
    public void setFromUid(String v) { this.fromUid = v; }
    public String getFromUsername() { return fromUsername; }
    public void setFromUsername(String v) { this.fromUsername = v; }
    public String getToUid() { return toUid; }
    public void setToUid(String v) { this.toUid = v; }
    public String getToUsername() { return toUsername; }
    public void setToUsername(String v) { this.toUsername = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
}