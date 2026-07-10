package com.team5.reflextrainer.data;

import android.content.Context;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TrainingSessionRepository {
    private final TrainingSessionDao trainingSessionDao;
    private final ExecutorService databaseExecutor;

    public TrainingSessionRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        trainingSessionDao = database.trainingSessionDao();
        databaseExecutor = Executors.newSingleThreadExecutor();
    }

    public void saveTrainingSession(String userId, int reactionTimeMs, String trainingMode, String difficulty) {
        TrainingSession session = new TrainingSession(
                userId,
                reactionTimeMs,
                trainingMode,
                difficulty,
                System.currentTimeMillis()
        );

        databaseExecutor.execute(() -> trainingSessionDao.insertTrainingSession(session));
    }

    public List<TrainingSession> getTrainingHistory() {
        return trainingSessionDao.getAllSessions();
    }

    public List<TrainingSession> getTrainingHistoryForUser(String userId) {
        return trainingSessionDao.getSessionsForUser(userId);
    }

    public void deleteTrainingSession(TrainingSession session) {
        databaseExecutor.execute(() -> trainingSessionDao.deleteTrainingSession(session));
    }

    public void clearTrainingHistory() {
        databaseExecutor.execute(trainingSessionDao::clearAllSessions);
    }
}
