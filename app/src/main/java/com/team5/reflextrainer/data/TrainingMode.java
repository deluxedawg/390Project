package com.team5.reflextrainer.data;

/** Which game mode produced a {@link TrainingSession} row. */
public enum TrainingMode {
    REACTION("Reaction"), RHYTHM("Rhythm"), FATIGUE("Fatigue");

    public final String label;

    TrainingMode(String label) {
        this.label = label;
    }
}
