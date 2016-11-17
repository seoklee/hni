package org.hni.events.service.om;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum EventState {

    STATE_REGISTER_START(100, EventName.REGISTER),
    STATE_REGISTER_GET_NAME(101, EventName.REGISTER),
    STATE_REGISTER_GET_EMAIL(102, EventName.REGISTER),
    STATE_REGISTER_CONFIRM_EMAIL(103, EventName.REGISTER),
    STATE_REGISTER_GET_AUTH_CODE(104, EventName.REGISTER),
    STATE_REGISTER_COMPLETE(105, EventName.REGISTER);

    private static final Map<EventName, EventState> INITIAL_STATES;
    private static final Map<EventName, EventState> END_STATES;
    private static final Map<Integer, EventState> ALL_STATES;

    static {
        INITIAL_STATES = new HashMap<>();
        INITIAL_STATES.put(EventName.REGISTER, STATE_REGISTER_START);

        END_STATES = new HashMap<>();
        END_STATES.put(EventName.REGISTER, STATE_REGISTER_COMPLETE);

        ALL_STATES = new HashMap<>();
        Arrays.stream(values()).forEach(s -> ALL_STATES.put(s.getStateCode(), s));
    }

    int stateCode;
    EventName eventName;

    EventState(int stateCode, EventName eventName) {
        this.stateCode = stateCode;
        this.eventName = eventName;
    }

    public EventName getEventName() {
        return eventName;
    }

    public int getStateCode() {
        return stateCode;
    }

    public static EventState getInitalState(final EventName eventName) {
        return INITIAL_STATES.get(eventName);
    }

    public static EventState fromStateCode(final int stateCode) {
        return ALL_STATES.get(stateCode);
    }
}
