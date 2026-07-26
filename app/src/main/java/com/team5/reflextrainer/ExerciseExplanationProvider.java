package com.team5.reflextrainer;

public class ExerciseExplanationProvider {

    public static class Explanation {
        private final String name;
        private final String howItWorks;
        private final String whatItTrains;
        private final String tip;

        public Explanation(String name, String howItWorks, String whatItTrains, String tip) {
            this.name = name;
            this.howItWorks = howItWorks;
            this.whatItTrains = whatItTrains;
            this.tip = tip;
        }

        public String getName() { return name; }
        public String getHowItWorks() { return howItWorks; }
        public String getWhatItTrains() { return whatItTrains; }
        public String getTip() { return tip; }
    }

    public Explanation forDifficulty(String difficulty) {
        String safe = difficulty == null ? "" : difficulty.trim().toLowerCase();
        switch (safe) {
            case "easy":
                return new Explanation(
                        "Easy — Reaction Basics",
                        "The sensor prompts at relaxed, predictable intervals. Tap as soon as the cue fires.",
                        "Baseline reaction speed and clean technique without time pressure.",
                        "Focus on reacting to the cue itself, not on anticipating it. Guessing early hurts your accuracy."
                );
            case "hard":
                return new Explanation(
                        "Hard — Pressure Rounds",
                        "Prompts arrive at short, randomized intervals with less recovery time between rounds.",
                        "Sustained focus and reaction speed under fatigue — closest to real reactive scenarios.",
                        "If accuracy drops below 80%, drop back to Medium. Speed without accuracy doesn't transfer."
                );
            case "medium":
            default:
                return new Explanation(
                        "Medium — Standard Training",
                        "Prompts arrive at moderately randomized intervals so you can't fall into a rhythm.",
                        "The balance of speed and consistency — this is where most measurable improvement happens.",
                        "Aim to keep every round within 100 ms of your best rather than chasing one fast outlier."
                );
        }
    }
}