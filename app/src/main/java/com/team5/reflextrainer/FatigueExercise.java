package com.team5.reflextrainer;

/** One exercise prompt shown between reaction-time tests in Fatigue Training. */
public class FatigueExercise {
    public final String name;
    public final boolean timed;   // true = countdown to 0, false = rep target + "I'm done" button
    public final int seconds;     // used when timed
    public final int reps;        // used when !timed
    /** Slug for assets/exercises/{slug}.mp4 — a looping demo clip shown above the prompt if present. */
    public final String demoAsset;

    public static FatigueExercise timed(String name, int seconds, String demoAsset) {
        return new FatigueExercise(name, true, seconds, 0, demoAsset);
    }

    public static FatigueExercise reps(String name, int reps, String demoAsset) {
        return new FatigueExercise(name, false, 0, reps, demoAsset);
    }

    private FatigueExercise(String name, boolean timed, int seconds, int reps, String demoAsset) {
        this.name = name;
        this.timed = timed;
        this.seconds = seconds;
        this.reps = reps;
        this.demoAsset = demoAsset;
    }
}
