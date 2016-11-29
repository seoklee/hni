package org.hni.events.service.dao;

import org.hni.events.service.om.EventName;
import org.hni.events.service.om.RegistrationState;
import org.hni.events.service.om.RegistrationStep;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.transaction.Transactional;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:test-applicationContext.xml"})
@Transactional
public class RegisterStateDAOTest {

    @Inject
    private RegistrationStateDAO registrationStateDAO;

    private static final String PHONE_NUMBER = "8188461238";

    @Test
    public void testGetByPhoneNumber() {
        RegistrationState initState = new RegistrationState(null, EventName.REGISTER, PHONE_NUMBER, null, RegistrationStep.STATE_REGISTER_START);
        registrationStateDAO.insert(initState);

        RegistrationState result = registrationStateDAO.getByPhoneNumber(PHONE_NUMBER);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(result.getEventName(), initState.getEventName());
        assertEquals(result.getPhoneNumber(), initState.getPhoneNumber());
        assertEquals(result.getPayload(), initState.getPayload());
        assertEquals(result.getRegistrationStep(), initState.getRegistrationStep());
    }


    @Test
    public void testUpdate() {
        RegistrationState initState = new RegistrationState(null,
            EventName.REGISTER, PHONE_NUMBER,null,
            RegistrationStep.STATE_REGISTER_START);

        registrationStateDAO.insert(initState);

        RegistrationState updated =  new RegistrationState(initState.getId(),
            initState.getEventName(), initState.getPhoneNumber(), "hello",
            RegistrationStep.STATE_REGISTER_GET_FIRST_NAME);

        registrationStateDAO.update(updated);

        RegistrationState result = registrationStateDAO.getByPhoneNumber(PHONE_NUMBER);

        assertNotNull(result);
        assertEquals(result.getId(), initState.getId());
        assertEquals(result.getEventName(), initState.getEventName());
        assertEquals(result.getPhoneNumber(), initState.getPhoneNumber());
        assertEquals(result.getPayload(), "hello");
        assertEquals(RegistrationStep.STATE_REGISTER_GET_FIRST_NAME, result.getRegistrationStep());
    }

    @Test
    public void testDeleteByPhoneNumber() {
        RegistrationState initState = new RegistrationState(null,
            EventName.REGISTER, PHONE_NUMBER,null,
            RegistrationStep.STATE_REGISTER_START);

        registrationStateDAO.insert(initState);

        RegistrationState updated =  new RegistrationState(initState.getId(),
            initState.getEventName(), initState.getPhoneNumber(), "hello",
            RegistrationStep.STATE_REGISTER_GET_FIRST_NAME);

        registrationStateDAO.update(updated);

        RegistrationState result = registrationStateDAO.getByPhoneNumber(PHONE_NUMBER);

        assertNotNull(result);
        assertEquals(result.getId(), initState.getId());
        assertEquals(result.getEventName(), initState.getEventName());
        assertEquals(result.getPhoneNumber(), initState.getPhoneNumber());
        assertEquals(result.getPayload(), "hello");
        assertEquals(RegistrationStep.STATE_REGISTER_GET_FIRST_NAME, result.getRegistrationStep());
    }
}
