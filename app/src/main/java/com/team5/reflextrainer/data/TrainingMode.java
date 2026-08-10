package com.team5.reflextrainer.data;

/** Which game mode produced a {@link TrainingSession} row. */
public enum TrainingMode {
    REACTION("Reaction"), RHYTHM("Rhythm"), FATIGUE("Fatigue"),SIMON_CUMULATIVE("Simon (Cumulative)");

    public final String label;

    TrainingMode(String label) {
        this.label = label;
    }
}
