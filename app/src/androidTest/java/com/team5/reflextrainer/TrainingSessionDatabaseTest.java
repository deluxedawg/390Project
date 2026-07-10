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
        trainingSessionDao.insertTrainingSession(
                new TrainingSession("test-user-1", 245, "Reaction Test", "Easy", 1000L)
        );
        trainingSessionDao.insertTrainingSession(
                new TrainingSession("test-user-1", 231, "Reaction Test", "Medium", 2000L)
        );
        trainingSessionDao.insertTrainingSession(
                new TrainingSession("test-user-2", 267, "Reaction Test", "Easy", 3000L)
        );

        List<TrainingSession> allSessions = trainingSessionDao.getAllSessions();
        List<TrainingSession> userOneSessions = trainingSessionDao.getSessionsForUser("test-user-1");

        assertEquals(3, allSessions.size());
        assertEquals(2, userOneSessions.size());
        assertEquals(231, userOneSessions.get(0).getReactionTimeMs());
    }

    @Test
    public void deleteAndClearTrainingSessions() {
        TrainingSession session = new TrainingSession("test-user-1", 245, "Reaction Test", "Easy", 1000L);
        long sessionId = trainingSessionDao.insertTrainingSession(session);
        session.setSessionId(sessionId);

        trainingSessionDao.deleteTrainingSession(session);
        assertEquals(0, trainingSessionDao.getAllSessions().size());

        trainingSessionDao.insertTrainingSession(
                new TrainingSession("test-user-1", 231, "Reaction Test", "Medium", 2000L)
        );
        trainingSessionDao.insertTrainingSession(
                new TrainingSession("test-user-2", 267, "Reaction Test", "Easy", 3000L)
        );

        trainingSessionDao.clearAllSessions();
        assertEquals(0, trainingSessionDao.getAllSessions().size());
    }
}
