package org.hni.events.service;

import org.hni.events.service.dao.SessionStateDao;
import org.hni.events.service.om.Event;
import org.hni.events.service.om.EventName;
import org.hni.events.service.om.EventState;
import org.hni.events.service.om.SessionState;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

public class RegisterServiceUnitTest {

    private static final String SESSION_ID = "1";
    private static final String PHONE_NUMBER = "8188461238";

    @InjectMocks
    private RegisterService registerService;

    @Mock
    private SessionStateDao sessionStateDao;

    private Event event;
    private SessionState state = null;
    private String payload = null;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        event = new Event(SESSION_ID, PHONE_NUMBER, "message");
    }

    @Test
    public void testStartRegister() throws Exception {
        state = new SessionState(EventName.REGISTER, SESSION_ID, PHONE_NUMBER, payload, EventState.STATE_REGISTER_START);
        when(sessionStateDao.get(eq(SESSION_ID))).thenReturn(state);
        String returnString = registerService.handleEvent(event);
        Assert.assertEquals("Welcome to Hunger Not Impossible! Msg & data rates may apply. "
                + "Any information you provide here will be kept private. "
                + "Reply with PRIVACY to learn more. Let's get you registered. What's your name?", returnString);
        verify(sessionStateDao, times(1)).update(any(SessionState.class));
    }

    @Test
    public void testGetName() throws Exception {
        state = new SessionState(EventName.REGISTER, SESSION_ID, PHONE_NUMBER, payload, EventState.STATE_REGISTER_GET_NAME);
        event.setTextMessage("firstname lastname");
        when(sessionStateDao.get(eq(SESSION_ID))).thenReturn(state);
        String returnString = registerService.handleEvent(event);
        Assert.assertEquals("Perfect! Lastly, I'd like to get your email address "
                + "to verify your account in case you text me from a new "
                + "number. So what's your email address? Thanks", returnString);
        verify(sessionStateDao, times(1)).update(any(SessionState.class));
    }

    @Test
    public void testGetEmail() throws Exception {
        state = new SessionState(EventName.REGISTER, SESSION_ID, PHONE_NUMBER, payload, EventState.STATE_REGISTER_GET_EMAIL);
        event.setTextMessage("johndoe@gmail.com");
        when(sessionStateDao.get(eq(SESSION_ID))).thenReturn(state);
        String returnString = registerService.handleEvent(event);
        Assert.assertEquals("Okay! I have " + "johndoe@gmail.com" + " as your email address. "
                + "Is that correct? Reply 1 for yes and 2 for no", returnString);
        verify(sessionStateDao, times(1)).update(any(SessionState.class));
    }

    @Test
    public void testConfirmEmail() throws Exception {
        state = new SessionState(EventName.REGISTER, SESSION_ID, PHONE_NUMBER, payload, EventState.STATE_REGISTER_CONFIRM_EMAIL);
        event.setTextMessage("1");
        when(sessionStateDao.get(eq(SESSION_ID))).thenReturn(state);
        String returnString = registerService.handleEvent(event);
        Assert.assertEquals("Please enter the 6 digit authorization code provided to you for this program.", returnString);
        verify(sessionStateDao, times(1)).update(any(SessionState.class));
    }

    @Test
    public void testGetAuthCode() throws Exception {
        state = new SessionState(EventName.REGISTER, SESSION_ID, PHONE_NUMBER, payload, EventState.STATE_REGISTER_GET_AUTH_CODE);
        event.setTextMessage("123456");
        when(sessionStateDao.get(eq(SESSION_ID))).thenReturn(state);
        String returnString = registerService.handleEvent(event);
        Assert.assertEquals("Ok. You're all setup for yourself.", returnString);
        verify(sessionStateDao, times(1)).update(any(SessionState.class));
    }

    @Test
    public void testCompleteRegister() throws Exception {
        state = new SessionState(EventName.REGISTER, SESSION_ID, PHONE_NUMBER, payload, EventState.STATE_REGISTER_COMPLETE);
        when(sessionStateDao.get(eq(SESSION_ID))).thenReturn(state);
        String returnString = registerService.handleEvent(event);
        Assert.assertNull(returnString);
        verify(sessionStateDao, times(1)).delete(eq(SESSION_ID));
    }
}
