package com.team5.reflextrainer;

import static org.junit.Assert.assertEquals;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.team5.reflextrainer.data.AppDatabase;
import com.team5.reflextrainer.data.TrainingSession;
import com.team5.reflextrainer.data.TrainingSessionDao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class TrainingSessionDatabaseTest {
    private AppDatabase database;
    private TrainingSessionDao trainingSessionDao;

    @Before
    public void setup() {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        trainingSessionDao = database.trainingSessionDao();
    }

    @After
    public void tearDown() throws IOException {
        database.close();
    }

    @Test
    public void insertAndReadTrainingSessions() {
        // (userId, avgMs, bestMs, totalRounds, correctRounds, difficulty, timestamp)
        trainingSessionDao.insertTrainingSession(
                new TrainingSession("test-user-1", 245, 210, 10, 9, "Easy", 1000L)
        );
        trainingSessionDao.insertTrainingSession(
                new TrainingSession("test-user-1", 231, 198, 10, 10, "Medium", 2000L)
        );
        trainingSessionDao.insertTrainingSession(
                new TrainingSession("test-user-2", 267, 240, 5, 4, "Easy", 3000L)
        );

        List<TrainingSession> allSessions = trainingSessionDao.getAllSessions();
        List<TrainingSession> userOneSessions = trainingSessionDao.getSessionsForUser("test-user-1");

        assertEquals(3, allSessions.size());
        assertEquals(2, userOneSessions.size());
        // ordered by timestamp DESC, so the newest (2000L) comes first
        assertEquals(231, userOneSessions.get(0).getAvgReactionMs());
        assertEquals(198, userOneSessions.get(0).getBestReactionMs());
        assertEquals(10, userOneSessions.get(0).getTotalRounds());
        assertEquals(10, userOneSessions.get(0).getCorrectRounds());
    }

    @Test
    public void deleteAndClearTrainingSessions() {
        TrainingSession session =
                new TrainingSession("test-user-1", 245, 210, 10, 9, "Easy", 1000L);
        long sessionId = trainingSessionDao.insertTrainingSession(session);
        session.setSessionId(sessionId);

        trainingSessionDao.deleteTrainingSession(session);
        assertEquals(0, trainingSessionDao.getAllSessions().size());

        trainingSessionDao.insertTrainingSession(
                new TrainingSession("test-user-1", 231, 198, 10, 10, "Medium", 2000L)
        );
        trainingSessionDao.insertTrainingSession(
                new TrainingSession("test-user-2", 267, 240, 5, 4, "Easy", 3000L)
        );

        trainingSessionDao.clearAllSessions();
        assertEquals(0, trainingSessionDao.getAllSessions().size());
    }
}