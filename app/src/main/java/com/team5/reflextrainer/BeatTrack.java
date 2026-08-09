package com.team5.reflextrainer;

import java.util.ArrayList;
import java.util.List;

public class BeatTrack {
    public final String name;
    public final int bpm;
    public final long firstBeatMs;   // when the first beat lands in the audio
    public final int totalBeats;
    public final int rawResId;       // R.raw.<file> for the backing track

    public BeatTrack(String name, int bpm, long firstBeatMs, int totalBeats, int rawResId) {
        this.name = name;
        this.bpm = bpm;
        this.firstBeatMs = firstBeatMs;
        this.totalBeats = totalBeats;
        this.rawResId = rawResId;
    }

    /** Generate the timestamp (ms into the track) of every beat to hit. */
    public List<Long> beatTimes() {
        List<Long> times = new ArrayList<>();
        long interval = Math.round(60000.0 / bpm);   // ms between beats
        for (int i = 0; i < totalBeats; i++) {
            times.add(firstBeatMs + i * interval);
        }
        return times;
    }
}