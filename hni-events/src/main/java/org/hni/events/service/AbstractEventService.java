package org.hni.events.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hni.events.service.dao.SessionStateDao;
import org.hni.events.service.om.Event;
import org.hni.events.service.om.EventState;

import javax.inject.Inject;
import java.io.IOException;

/**
 * Created by walmart on 11/14/16.
 * Handles events coming in to HNI.
 * For now these events will take the shape of SMS messages.
 * ### These are strings for now, but eventually should be Event POJOs
 */
public abstract class AbstractEventService<T> implements EventService {

    @Inject
    protected SessionStateDao sessionStateDao;

    private ObjectMapper objectMapper = new ObjectMapper();

    public abstract String handleEvent(Event event);

//    protected abstract String performRouting(EventState state, String textMessage, T object);

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
}
