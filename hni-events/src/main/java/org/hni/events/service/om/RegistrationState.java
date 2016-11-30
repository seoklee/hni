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
@Table(name = "registration_state")
public class RegistrationState implements Persistable, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    protected Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_name", nullable = false)
    private EventName eventName;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "payload")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_state", nullable = false)
    private RegistrationStep registrationStep;

    private RegistrationState() {}

    public static RegistrationState create(EventName eventName, String phoneNumber, String payload, RegistrationStep registrationStep) {
        RegistrationState registrationState = new RegistrationState();
        registrationState.eventName = eventName;
        registrationState.phoneNumber = phoneNumber;
        registrationState.payload = payload;
        registrationState.registrationStep = registrationStep;
        return registrationState;
    }

    @Override
    public Long getId() {
        return id;
    }

    public EventName getEventName() {
        return eventName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public RegistrationStep getRegistrationStep() {
        return registrationStep;
    }

    public String getPayload() {
        return payload;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setEventName(EventName eventName) {
        this.eventName = eventName;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public void setRegistrationStep(RegistrationStep registrationStep) {
        this.registrationStep = registrationStep;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RegistrationState that = (RegistrationState) o;

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
