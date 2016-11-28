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

    public RegistrationState() {
    }

    public RegistrationState(Long id, EventName eventName, String phoneNumber, String payload, RegistrationStep registrationStep) {
        this.id = id;
        this.eventName = eventName;
        this.phoneNumber = phoneNumber;
        this.payload = payload;
        this.registrationStep = registrationStep;
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
