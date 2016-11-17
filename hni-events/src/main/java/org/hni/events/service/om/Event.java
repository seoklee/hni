package org.hni.events.service.om;

/**
 * Created by walmart on 11/14/16.
 */
public class Event {

    private String sessionId;

    private String phoneNumber;

    private String textMessage;

    public Event(String sessionId, String phoneNumber, String textMessage) {
        this.sessionId = sessionId;
        this.phoneNumber = phoneNumber;
        this.textMessage = textMessage;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getTextMessage() {
        return textMessage;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setTextMessage(String textMessage) {
        this.textMessage = textMessage;
    }
}
