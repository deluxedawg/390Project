package com.team5.reflextrainer;

import java.util.List;

public class UserProfile {
    private String uid;
    private String username;
    private String email;
    private int avatarId;   // index into Avatars.DRAWABLES; 0 for existing accounts
    private List<Integer> earnedBadges;   // indices into Achievements.NAMES; null until the first badge syncs

    // BMI/reaction-time research opt-in; heightCm/weightKg are only meaningful when researchConsent is true
    private boolean researchConsent;
    private double heightCm;
    private double weightKg;

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

    public List<Integer> getEarnedBadges() { return earnedBadges; }
    public void setEarnedBadges(List<Integer> earnedBadges) { this.earnedBadges = earnedBadges; }

    public boolean isResearchConsent() { return researchConsent; }
    public void setResearchConsent(boolean researchConsent) { this.researchConsent = researchConsent; }

    public double getHeightCm() { return heightCm; }
    public void setHeightCm(double heightCm) { this.heightCm = heightCm; }

    public double getWeightKg() { return weightKg; }
    public void setWeightKg(double weightKg) { this.weightKg = weightKg; }

    /** BMI = kg / m^2. Only meaningful when researchConsent is true and both values are set. */
    public double getBmi() {
        if (heightCm <= 0 || weightKg <= 0) return 0;
        double heightM = heightCm / 100.0;
        return weightKg / (heightM * heightM);
    }

    /** Standard WHO adult BMI bands. Returns "" when there's no BMI to classify. */
    public String getBmiCategory() {
        return categoryFor(getBmi());
    }

    public static String categoryFor(double bmi) {
        if (bmi <= 0) return "";
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25) return "Normal";
        if (bmi < 30) return "Overweight";
        return "Obese";
    }
}