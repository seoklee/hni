package org.hni.events.service.om;

import org.hni.common.om.Persistable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "session_state")
public class SessionState implements Persistable, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    protected Long id;

    @Column(name = "sessionid", nullable = false)
    private String sessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "eventname", nullable = false)
    private EventName eventName;

    @Column(name = "phoneno", nullable = false)
    private String phoneNumber;

    @Column(name = "payload")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "eventstate", nullable = false)
    private EventState eventState;

    public SessionState() {
    }

    public SessionState(EventName eventName, String sessionId, String phoneNumber) {
        this.sessionId = sessionId;
        this.eventName = eventName;
        this.phoneNumber = phoneNumber;
        payload = null;
        eventState = EventState.getInitalState(eventName);
    }

    public SessionState(EventName eventName, String sessionId, String phoneNumber, String payload, EventState eventState) {
        this.sessionId = sessionId;
        this.eventName = eventName;
        this.phoneNumber = phoneNumber;
        this.payload = payload;
        this.eventState = eventState;
    }

    @Override
    public Long getId() {
        return id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public EventName getEventName() {
        return eventName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public EventState getEventState() {
        return eventState;
    }

    public String getPayload() {
        return payload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SessionState that = (SessionState) o;

        if (id == null) {
            if (that.id != null)
                return false;
        } else if (!id.equals(that.id))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }
}
