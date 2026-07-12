package com.team5.reflextrainer.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TrainingSessionDao {
    @Insert
    long insertTrainingSession(TrainingSession session);

    @Query("SELECT * FROM training_sessions ORDER BY timestamp DESC")
    List<TrainingSession> getAllSessions();

    @Query("SELECT * FROM training_sessions WHERE userId = :userId ORDER BY timestamp DESC")
    List<TrainingSession> getSessionsForUser(String userId);

    @Delete
    void deleteTrainingSession(TrainingSession session);

    @Query("DELETE FROM training_sessions")
    void clearAllSessions();
}
