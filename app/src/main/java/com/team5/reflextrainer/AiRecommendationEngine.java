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

    /**
     * Suggests a starting difficulty ("Easy"/"Medium"/"Hard") for the next Reaction session
     * from the accuracy and average reaction time of the user's most recent sessions.
     * Returns null when there isn't enough history yet to base a suggestion on.
     */
    public String recommendDifficulty(List<TrainingSession> recentReactionSessions) {
        if (recentReactionSessions == null || recentReactionSessions.isEmpty()) return null;

        int sumMs = 0, msCount = 0, totalRounds = 0, correctRounds = 0, taken = 0;
        for (TrainingSession session : recentReactionSessions) {
            if (taken >= 3) break;   // recentReactionSessions is newest-first
            if (session.getAvgReactionMs() > 0) {
                sumMs += session.getAvgReactionMs();
                msCount++;
            }
            totalRounds += session.getTotalRounds();
            correctRounds += session.getCorrectRounds();
            taken++;
        }
        if (msCount == 0 || totalRounds == 0) return null;

        int avgMs = Math.round(sumMs / (float) msCount);
        int accuracy = Math.round((correctRounds * 100f) / totalRounds);

        if (accuracy < 70) return "Easy";
        if (accuracy >= 85 && avgMs <= 350) return "Hard";
        return "Medium";
    }

    public Recommendation buildProgressSummary(List<TrainingSession> history) {
        if (history == null || history.size() < 2) {
            return new Recommendation(
                    "Not enough data yet",
                    "Complete at least two sessions and the coach will start tracking your trend."
            );
        }

        // NOTE: assumes history is sorted newest-first — same assumption
        // (and same risk) as averageRecent(). Verify against your DATA-3 query.
        int newest = firstValidAvg(history, 0, 3);          // avg of up to 3 newest
        int oldest = firstValidAvg(history, history.size() - 3, 3); // avg of up to 3 oldest

        if (newest <= 0 || oldest <= 0) {
            return new Recommendation("Not enough valid sessions",
                    "Finish a few sessions with completed rounds to unlock trend tracking.");
        }

        int delta = oldest - newest; // positive = faster now
        if (delta >= 25) {
            return new Recommendation("You're getting faster",
                    "Your average has improved by about " + delta + " ms since you started. Keep the same routine.");
        }
        if (delta <= -25) {
            return new Recommendation("Pace has slipped",
                    "Your average is about " + (-delta) + " ms slower than earlier sessions. Shorter, more frequent sets usually fix this.");
        }
        return new Recommendation("Holding steady",
                "Your average is stable. To break the plateau, raise the difficulty for one session per week.");
    }

    private int firstValidAvg(List<TrainingSession> history, int startIndex, int take) {
        int sum = 0, count = 0;
        for (int i = Math.max(0, startIndex); i < history.size() && count < take; i++) {
            int v = history.get(i).getAvgReactionMs();
            if (v > 0) { sum += v; count++; }
        }
        return count == 0 ? 0 : Math.round(sum / (float) count);
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
