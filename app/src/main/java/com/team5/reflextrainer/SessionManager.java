package com.team5.reflextrainer;

import java.util.ArrayList;
import java.util.List;

public class SessionManager {
    private static SessionManager instance;
    private List<SessionCls> sessions;

    private SessionManager() {
        sessions = new ArrayList<>();
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void addSession(SessionCls session) {
        sessions.add(session);
    }

    public List<SessionCls> getSessions() {
        return new ArrayList<>(sessions);
    }
}
