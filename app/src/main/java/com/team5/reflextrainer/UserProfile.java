package com.team5.reflextrainer;

public class UserProfile {
    private String uid;
    private String username;
    private String email;
    private int avatarId;   // index into Avatars.DRAWABLES; 0 for existing accounts

    public UserProfile() { }   // required by Firestore

    public UserProfile(String uid, String username, String email) {
        this.uid = uid;
        this.username = username;
        this.email = email;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getAvatarId() { return avatarId; }
    public void setAvatarId(int avatarId) { this.avatarId = avatarId; }
}