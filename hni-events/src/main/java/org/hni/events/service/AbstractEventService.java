package org.hni.events.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hni.events.service.dao.SessionStateDao;
import org.hni.events.service.om.Event;
import org.hni.events.service.om.EventState;
import org.hni.events.service.om.SessionState;

import javax.inject.Inject;
import java.io.IOException;

/**
 * Created by walmart on 11/14/16.
 * Handles events coming in to HNI.
 * For now these events will take the shape of SMS messages.
 * ### These are strings for now, but eventually should be Event POJOs
 */
public abstract class AbstractEventService<T> implements EventService {

    protected SessionStateDao sessionStateDao;

    private ObjectMapper objectMapper = new ObjectMapper();

    public String handleEvent(Event event) {
        final SessionState state = sessionStateDao.get(event.getSessionId());
        // perform the workflow logic
        final WorkFlowStepResult stepResult = performWorkFlowStep(event, state);

        if (!state.getEventState().equals(stepResult.getNextStateCode())) {
            final SessionState nextState =
                    new SessionState(state.getEventName(), state.getSessionId(), state.getPhoneNumber(),
                            stepResult.getPayload(), stepResult.getNextStateCode());
            sessionStateDao.update(nextState);
        }
        return stepResult.getReturnString();
    }

    protected abstract WorkFlowStepResult performWorkFlowStep(Event event, SessionState currentState);

    protected T deserialize(final String payload, final Class<T> clazz) {
        try {
            return objectMapper.readValue(payload, clazz);
        } catch (IOException ex) {
            throw new RuntimeException();
        }
    }

    protected String serialize(final T object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (IOException ex) {
            throw new RuntimeException();
        }
    }

    @Inject
    public void setSessionStateDao(final SessionStateDao sessionStateDao) {
        this.sessionStateDao = sessionStateDao;
    }

    protected static class WorkFlowStepResult {

        private String returnString;
        private EventState nextStateCode;
        private String payload;

        public WorkFlowStepResult(String returnString, EventState nextStateCode, String payload) {
            this.returnString = returnString;
            this.nextStateCode = nextStateCode;
            this.payload = payload;
        }

        public String getReturnString() {
            return returnString;
        }

        public EventState getNextStateCode() {
            return nextStateCode;
        }

        public String getPayload() {
            return payload;
        }
    }
}
