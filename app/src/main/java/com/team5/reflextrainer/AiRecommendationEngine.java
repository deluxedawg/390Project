package com.team5.reflextrainer;

import com.team5.reflextrainer.data.TrainingSession;

import java.util.List;

public class AiRecommendationEngine {

    public static class Recommendation {
        private final String title;
        private final String detail;

        public Recommendation(String title, String detail) {
            this.title = title;
            this.detail = detail;
        }

        public String getTitle() { return title; }
        public String getDetail() { return detail; }
    }

    public Recommendation build(int avgMs, int bestMs, int totalRounds, int correctRounds,
                                String difficulty, List<TrainingSession> history) {
        int accuracy = totalRounds > 0 ? Math.round((correctRounds * 100f) / totalRounds) : 0;
        String safeDifficulty = (difficulty == null || difficulty.trim().isEmpty()) ? "Medium" : difficulty;

        if (avgMs <= 0) {
            return new Recommendation(
                    "Build a clean baseline",
                    "Try another " + safeDifficulty + " session and focus on completing valid rounds. The coach needs a saved average before it can compare your progress."
            );
        }

        int recentAvg = averageRecent(history, avgMs);
        int bestRecent = bestRecent(history, bestMs);

        if (accuracy < 70) {
            return new Recommendation(
                    "Focus on accuracy first",
                    "You finished at " + accuracy + "% accuracy. Drop one difficulty level or use shorter 5-round sets until you can reach at least 80% correct."
            );
        }

        if (avgMs - bestMs >= 120 && bestMs > 0) {
            return new Recommendation(
                    "Close the consistency gap",
                    "Your best round was " + bestMs + " ms, but your average was " + avgMs + " ms. Stay on " + safeDifficulty + " and aim to keep every round within 100 ms of your best."
            );
        }

        if (history != null && history.size() >= 3 && avgMs > recentAvg + 40) {
            return new Recommendation(
                    "Reset before pushing speed",
                    "This session was slower than your recent pace of about " + recentAvg + " ms. Take a short rest, then run a 5-round set for cleaner reactions."
            );
        }

        if (avgMs <= 300 && accuracy >= 85) {
            return new Recommendation(
                    "Ready for a harder set",
                    "Average " + avgMs + " ms with " + accuracy + "% accuracy is strong. Move up one difficulty level or increase the round count for your next session."
            );
        }

        if (history == null || history.size() < 3) {
            return new Recommendation(
                    "Collect more training data",
                    "Good start. Complete two more sessions with the same settings so the coach can detect whether your average is improving."
            );
        }

        return new Recommendation(
                "Keep building controlled speed",
                "Your recent best is " + bestRecent + " ms. Stay on " + safeDifficulty + " and try to improve your average by 25 ms while keeping accuracy above 80%."
        );
    }

    private int averageRecent(List<TrainingSession> history, int fallback) {
        if (history == null || history.isEmpty()) return fallback;

        int count = 0;
        int sum = 0;
        for (TrainingSession session : history) {
            if (session.getAvgReactionMs() > 0) {
                sum += session.getAvgReactionMs();
                count++;
            }
            if (count == 3) break;
        }
        return count == 0 ? fallback : Math.round(sum / (float) count);
    }

    private int bestRecent(List<TrainingSession> history, int fallback) {
        int best = fallback > 0 ? fallback : Integer.MAX_VALUE;
        if (history != null) {
            for (TrainingSession session : history) {
                if (session.getBestReactionMs() > 0 && session.getBestReactionMs() < best) {
                    best = session.getBestReactionMs();
                }
            }
        }
        return best == Integer.MAX_VALUE ? 0 : best;
    }
}
