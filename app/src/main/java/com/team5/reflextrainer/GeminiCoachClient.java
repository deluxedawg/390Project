package com.team5.reflextrainer;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.team5.reflextrainer.data.TrainingSession;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Generates AI Coach text via the free-tier Gemini API. Falls back to the
 * offline heuristic engine whenever no API key is configured or the request
 * fails for any reason, so the coach always has something to show.
 */
public class GeminiCoachClient {

    private static final String TAG = "GeminiCoach";
    private static final String MODEL = "gemini-flash-latest";
    private static final String ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL + ":generateContent";

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    public interface Callback {
        void onResult(AiRecommendationEngine.Recommendation recommendation);
    }

    public static void generate(int avgMs, int bestMs, int totalRounds, int correctRounds,
                                 String difficulty, List<TrainingSession> history, Callback callback) {
        AiRecommendationEngine.Recommendation fallback =
                new AiRecommendationEngine().build(avgMs, bestMs, totalRounds, correctRounds, difficulty, history);

        String apiKey = BuildConfig.GEMINI_API_KEY;
        if (apiKey == null || apiKey.trim().isEmpty()) {
            Log.w(TAG, "No GEMINI_API_KEY configured — using offline heuristic");
            callback.onResult(fallback);
            return;
        }

        EXECUTOR.execute(() -> {
            AiRecommendationEngine.Recommendation result;
            try {
                AiRecommendationEngine.Recommendation gemini = requestFromGemini(
                        apiKey, avgMs, bestMs, totalRounds, correctRounds, difficulty, history);
                if (gemini != null) {
                    Log.i(TAG, "Gemini responded successfully");
                    result = gemini;
                } else {
                    Log.w(TAG, "Gemini call did not return a usable result — using offline heuristic");
                    result = fallback;
                }
            } catch (Exception e) {
                Log.e(TAG, "Gemini request failed — using offline heuristic", e);
                result = fallback;
            }
            AiRecommendationEngine.Recommendation finalResult = result;
            MAIN_HANDLER.post(() -> callback.onResult(finalResult));
        });
    }

    private static AiRecommendationEngine.Recommendation requestFromGemini(
            String apiKey, int avgMs, int bestMs, int totalRounds, int correctRounds,
            String difficulty, List<TrainingSession> history) throws Exception {

        String prompt = buildPrompt(avgMs, bestMs, totalRounds, correctRounds, difficulty, history);

        JSONObject part = new JSONObject().put("text", prompt);
        JSONObject content = new JSONObject().put("parts", new JSONArray().put(part));
        JSONObject body = new JSONObject().put("contents", new JSONArray().put(content));

        URL url = new URL(ENDPOINT + "?key=" + apiKey);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(15000);
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }

        int status = conn.getResponseCode();
        InputStream stream = status >= 200 && status < 300 ? conn.getInputStream() : conn.getErrorStream();
        String responseBody = readStream(stream);
        conn.disconnect();

        if (status < 200 || status >= 300) {
            Log.w(TAG, "Gemini HTTP " + status + ": " + responseBody);
            return null;
        }

        JSONObject json = new JSONObject(responseBody);
        String text = json.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text");

        return parseRecommendation(text);
    }

    private static String buildPrompt(int avgMs, int bestMs, int totalRounds, int correctRounds,
                                       String difficulty, List<TrainingSession> history) {
        int accuracy = totalRounds > 0 ? Math.round((correctRounds * 100f) / totalRounds) : 0;
        String safeDifficulty = (difficulty == null || difficulty.trim().isEmpty()) ? "Medium" : difficulty;

        StringBuilder historyLine = new StringBuilder();
        if (history != null && !history.isEmpty()) {
            historyLine.append("Recent past sessions (newest first): ");
            int shown = 0;
            for (TrainingSession s : history) {
                if (shown >= 5) break;
                historyLine.append(String.format(
                        "[avg=%dms best=%dms acc=%d/%d diff=%s] ",
                        s.getAvgReactionMs(), s.getBestReactionMs(),
                        s.getCorrectRounds(), s.getTotalRounds(), s.getDifficulty()));
                shown++;
            }
        } else {
            historyLine.append("No prior session history available.");
        }

        return "You are an encouraging reflex-training coach inside a mobile app. "
                + "A user just finished a reaction-time training session with these results: "
                + "average reaction time " + avgMs + " ms, best reaction time " + bestMs + " ms, "
                + correctRounds + "/" + totalRounds + " correct rounds (" + accuracy + "% accuracy), "
                + "difficulty " + safeDifficulty + ". " + historyLine + " "
                + "Write a short, specific, motivating coaching tip based on these numbers and any trend "
                + "in the history. Respond with ONLY a JSON object, no markdown fences, no extra text, "
                + "in exactly this shape: {\"title\": \"a short 3-6 word headline\", "
                + "\"detail\": \"1-3 sentences of specific, actionable coaching advice\"}";
    }

    private static AiRecommendationEngine.Recommendation parseRecommendation(String text) {
        if (text == null) return null;
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end <= start) return null;
        try {
            JSONObject obj = new JSONObject(text.substring(start, end + 1));
            String title = obj.optString("title", "AI Coach");
            String detail = obj.optString("detail", "");
            if (detail.trim().isEmpty()) return null;
            return new AiRecommendationEngine.Recommendation(title, detail);
        } catch (Exception e) {
            return null;
        }
    }

    private static String readStream(InputStream stream) throws Exception {
        if (stream == null) return "";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = stream.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        return out.toString(StandardCharsets.UTF_8.name());
    }
}
