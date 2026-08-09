package com.team5.reflextrainer;

public class FriendRequest {
    private String fromUid;
    private String fromUsername;
    private int fromAvatarId;
    private String toUid;
    private String toUsername;
    private int toAvatarId;
    private String status;   // "pending" or "accepted"

    public FriendRequest() { }   // required by Firestore

    public FriendRequest(String fromUid, String fromUsername, int fromAvatarId,
                         String toUid, String toUsername, int toAvatarId, String status) {
        this.fromUid = fromUid;
        this.fromUsername = fromUsername;
        this.fromAvatarId = fromAvatarId;
        this.toUid = toUid;
        this.toUsername = toUsername;
        this.toAvatarId = toAvatarId;
        this.status = status;
    }

    public String getFromUid() { return fromUid; }
    public void setFromUid(String v) { this.fromUid = v; }
    public String getFromUsername() { return fromUsername; }
    public void setFromUsername(String v) { this.fromUsername = v; }
    public int getFromAvatarId() { return fromAvatarId; }
    public void setFromAvatarId(int v) { this.fromAvatarId = v; }
    public String getToUid() { return toUid; }
    public void setToUid(String v) { this.toUid = v; }
    public String getToUsername() { return toUsername; }
    public void setToUsername(String v) { this.toUsername = v; }
    public int getToAvatarId() { return toAvatarId; }
    public void setToAvatarId(int v) { this.toAvatarId = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
}