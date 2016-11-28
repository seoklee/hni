package org.hni.events.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hni.events.service.dao.RegistrationStateDAO;
import org.hni.events.service.om.Event;
import org.hni.events.service.om.EventName;
import org.hni.events.service.om.RegistrationStep;
import org.hni.events.service.om.RegistrationState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;

/**
 * Created by walmart on 11/14/16.
 * Handles Registration events.
 */
public abstract class AbstractRegistrationService<T> implements EventService{

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRegistrationService.class);

    protected RegistrationStateDAO registrationStateDAO;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String handleEvent(final Event event) {
        RegistrationState state = registrationStateDAO.getByPhoneNumber(event.getPhoneNumber());
        if (state == null) {
            // No state, so we're just beginning.
            state = new RegistrationState(null, EventName.REGISTER, event.getPhoneNumber(),
                    null, RegistrationStep.STATE_REGISTER_START);
            registrationStateDAO.insert(state);
        }
        LOGGER.info("Handling {} at state {} in {} flow", event, state.getRegistrationStep().getStateCode(),
                state.getEventName().name());

        // perform the workflow logic
        final WorkFlowStepResult stepResult = performWorkFlowStep(event, state);

        if (!state.getRegistrationStep().equals(stepResult.getNextStateCode())) {
            final RegistrationState nextState =
                    new RegistrationState(state.getId(), state.getEventName(), state.getPhoneNumber(),
                            stepResult.getPayload(), stepResult.getNextStateCode());
            registrationStateDAO.update(nextState);
        }
        return stepResult.getReturnString();
    }

    protected abstract WorkFlowStepResult performWorkFlowStep(Event event, RegistrationState currentState);

    protected T deserialize(final String payload, final Class<T> clazz) {
        try {
            return objectMapper.readValue(payload, clazz);
        } catch (IOException ex) {
            throw new RuntimeException("deserialize failed " + payload, ex);
        }
    }

    protected String serialize(final T object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (IOException ex) {
            throw new RuntimeException("serialize failed", ex);
        }
    }

    @Inject
    public void setRegistrationStateDAO(final RegistrationStateDAO registrationStateDAO) {
        this.registrationStateDAO = registrationStateDAO;
    }

    protected static class WorkFlowStepResult {

        private String returnString;
        private RegistrationStep nextStateCode;
        private String payload;

        public WorkFlowStepResult(String returnString, RegistrationStep nextStateCode, String payload) {
            this.returnString = returnString;
            this.nextStateCode = nextStateCode;
            this.payload = payload;
        }

        public String getReturnString() {
            return returnString;
        }

        public RegistrationStep getNextStateCode() {
            return nextStateCode;
        }

        public String getPayload() {
            return payload;
        }
    }
}
