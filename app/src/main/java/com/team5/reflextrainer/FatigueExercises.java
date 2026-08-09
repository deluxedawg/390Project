package com.team5.reflextrainer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** Exercise pools for Fatigue Training, grouped by the body part the user chooses to focus on. */
public final class FatigueExercises {
    private FatigueExercises() { }

    public enum Category {
        CARDIO("Cardio"), CORE("Core"), LEGS("Legs"), ARMS("Arms");

        public final String label;
        Category(String label) { this.label = label; }
    }

    private static final Map<Category, List<FatigueExercise>> POOLS = new EnumMap<>(Category.class);
    static {
        POOLS.put(Category.CARDIO, Arrays.asList(
                FatigueExercise.reps("Jumping Jacks", 25, "jumping_jacks"),
                FatigueExercise.timed("High Knees", 30, "high_knees"),
                FatigueExercise.timed("Butt Kicks", 30, "butt_kicks"),
                FatigueExercise.timed("Run in Place", 45, "run_in_place"),
                FatigueExercise.reps("Burpees", 8, "burpees")
        ));
        POOLS.put(Category.CORE, Arrays.asList(
                FatigueExercise.timed("Plank", 30, "plank"),
                FatigueExercise.reps("Sit-ups", 15, "situps"),
                FatigueExercise.reps("Mountain Climbers", 20, "mountain_climbers"),
                FatigueExercise.reps("Russian Twists", 20, "russian_twists"),
                FatigueExercise.reps("Bicycle Crunches", 20, "bicycle_crunches")
        ));
        POOLS.put(Category.LEGS, Arrays.asList(
                FatigueExercise.reps("Squats", 15, "squats"),
                FatigueExercise.reps("Lunges", 12, "lunges"),
                FatigueExercise.timed("Wall Sit", 30, "wall_sit"),
                FatigueExercise.reps("Calf Raises", 20, "calf_raises"),
                FatigueExercise.reps("Jump Squats", 10, "jump_squats")
        ));
        POOLS.put(Category.ARMS, Arrays.asList(
                FatigueExercise.reps("Push-ups", 12, "pushups"),
                FatigueExercise.reps("Tricep Dips", 12, "tricep_dips"),
                FatigueExercise.timed("Arm Circles", 20, "arm_circles"),
                FatigueExercise.reps("Plank Shoulder Taps", 20, "plank_shoulder_taps"),
                FatigueExercise.reps("Diamond Push-ups", 8, "diamond_pushups")
        ));
    }

    /** 3 distinct exercises from the category's pool, in random order. */
    public static List<FatigueExercise> pickThree(Category category, Random random) {
        List<FatigueExercise> pool = new ArrayList<>(POOLS.get(category));
        Collections.shuffle(pool, random);
        return pool.subList(0, 3);
    }
}
