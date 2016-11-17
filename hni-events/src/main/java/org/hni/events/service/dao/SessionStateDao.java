package org.hni.events.service.dao;

import org.hni.events.service.om.SessionState;

public interface SessionStateDao {

    SessionState get(String sessionId);

    boolean insert(SessionState state);

    boolean update(SessionState state);

    boolean delete(String sessionId);

}
