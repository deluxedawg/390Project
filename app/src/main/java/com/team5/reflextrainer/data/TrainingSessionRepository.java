package com.team5.reflextrainer.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TrainingSessionRepository {
    private final TrainingSessionDao trainingSessionDao;
    private final ExecutorService databaseExecutor;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public TrainingSessionRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        trainingSessionDao = database.trainingSessionDao();
        databaseExecutor = Executors.newSingleThreadExecutor();
    }

    /** DATA-2: save a completed session for the given user. */
    public void saveTrainingSession(String userId, long durationMs, String status) {
        TrainingSession session = new TrainingSession(
                userId, durationMs, status, System.currentTimeMillis());
        databaseExecutor.execute(() -> trainingSessionDao.insertTrainingSession(session));
    }

    public interface HistoryCallback {
        void onResult(List<TrainingSession> sessions);
    }

    /** DATA-1/DATA-3: read this user's sessions off the UI thread. */
    public void getTrainingHistoryForUser(String userId, HistoryCallback callback) {
        databaseExecutor.execute(() -> {
            List<TrainingSession> sessions = trainingSessionDao.getSessionsForUser(userId);
            mainHandler.post(() -> callback.onResult(sessions));
        });
    }

    public void deleteTrainingSession(TrainingSession session) {
        databaseExecutor.execute(() -> trainingSessionDao.deleteTrainingSession(session));
    }

    public void clearTrainingHistory() {
        databaseExecutor.execute(trainingSessionDao::clearAllSessions);
    }
}
