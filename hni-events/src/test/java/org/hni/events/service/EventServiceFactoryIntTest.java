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
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class EventServiceFactoryIntTest {

    private static final String SESSION_ID = "1";
    private static final String PHONE_NUMBER = "8188461238";

    @InjectMocks
    private EventServiceFactory factory;

    @Mock
    private SessionStateDao sessionStateDao;

    @Spy
    private RegisterService registerService;

    private Map<String, SessionState> sessionStateDbMock = new HashMap<>();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        factory.init();
        registerService.setSessionStateDao(sessionStateDao);
        doCallRealMethod().when(registerService).handleEvent(any(Event.class));

        sessionStateDbMock.put(SESSION_ID, new SessionState(EventName.REGISTER, SESSION_ID, PHONE_NUMBER, null, EventState.STATE_REGISTER_START));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                SessionState insertState = (SessionState) invocationOnMock.getArguments()[0];
                sessionStateDbMock.put(insertState.getSessionId(), insertState);
                return true;
            }
        }).when(sessionStateDao).insert(any(SessionState.class));


        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return sessionStateDbMock.get(invocationOnMock.getArguments()[0]);
            }
        }).when(sessionStateDao).get(eq(SESSION_ID));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                SessionState updatedState = (SessionState) invocationOnMock.getArguments()[0];
                sessionStateDbMock.put(updatedState.getSessionId(), updatedState);
                return true;
            }
        }).when(sessionStateDao).update(any(SessionState.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                String sessionId = (String) invocationOnMock.getArguments()[0];
                sessionStateDbMock.remove(sessionId);
                return true;
            }
        }).when(sessionStateDao).delete(any(String.class));
    }

    @Test
    public void testStartRegister() throws Exception {
        doReturn(null).doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return sessionStateDbMock.get(invocationOnMock.getArguments()[0]);
            }
        }).when(sessionStateDao).get(eq(SESSION_ID));

        String returnString = factory.handleEvent(new Event(SESSION_ID, PHONE_NUMBER, "REGISTER"));
        Assert.assertEquals("Welcome to Hunger Not Impossible! Msg & data rates may apply. "
                + "Any information you provide here will be kept private. "
                + "Reply with PRIVACY to learn more. Let's get you registered. What's your name?", returnString);
        verify(sessionStateDao, times(1)).insert(any(SessionState.class));
        verify(sessionStateDao, times(1)).update(any(SessionState.class));
        SessionState nextState = sessionStateDao.get(SESSION_ID);
        Assert.assertEquals(SESSION_ID, nextState.getSessionId());
        Assert.assertEquals(PHONE_NUMBER, nextState.getPhoneNumber());
        Assert.assertEquals(EventName.REGISTER, nextState.getEventName());
        Assert.assertEquals(EventState.STATE_REGISTER_GET_NAME, nextState.getEventState());
    }

    @Test
    public void testInterruptExistingWorkFlow() {
        // existing workflow to MEAL
        sessionStateDbMock.put(SESSION_ID, new SessionState(EventName.MEAL, SESSION_ID, PHONE_NUMBER, null, null));
        String returnString = factory.handleEvent(new Event(SESSION_ID, PHONE_NUMBER, "REGISTER"));
        Assert.assertEquals("Welcome to Hunger Not Impossible! Msg & data rates may apply. "
                + "Any information you provide here will be kept private. "
                + "Reply with PRIVACY to learn more. Let's get you registered. What's your name?", returnString);
        verify(sessionStateDao, times(1)).delete(eq(SESSION_ID));
        verify(sessionStateDao, times(1)).insert(any(SessionState.class));
    }

    @Test
    public void testRegisterWorkFlow() throws Exception {
        doReturn(null).doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return sessionStateDbMock.get(invocationOnMock.getArguments()[0]);
            }
        }).when(sessionStateDao).get(eq(SESSION_ID));

        // enroll keyword to start register workflow
        String returnString = factory.handleEvent(new Event(SESSION_ID, PHONE_NUMBER, "ENROLL"));
        Assert.assertEquals("Welcome to Hunger Not Impossible! Msg & data rates may apply. "
                + "Any information you provide here will be kept private. "
                + "Reply with PRIVACY to learn more. Let's get you registered. What's your name?", returnString);
        // name
        returnString = factory.handleEvent(new Event(SESSION_ID, PHONE_NUMBER, "john doe"));
        Assert.assertEquals("Perfect! Lastly, I'd like to get your email address "
                + "to verify your account in case you text me from a new "
                + "number. So what's your email address? Thanks", returnString);
        // email
        returnString = factory.handleEvent(new Event(SESSION_ID, PHONE_NUMBER, "johndoe@gmail.com"));
        Assert.assertEquals("Okay! I have " + "johndoe@gmail.com" + " as your email address. "
                + "Is that correct? Reply 1 for yes and 2 for no", returnString);
        // confirm email
        returnString = factory.handleEvent(new Event(SESSION_ID, PHONE_NUMBER, "1"));
        Assert.assertEquals("Please enter the 6 digit authorization code provided to you for this program.", returnString);
        // auth code
        returnString = factory.handleEvent(new Event(SESSION_ID, PHONE_NUMBER, "123456"));
        Assert.assertEquals("Ok. You're all setup for yourself. If you have additional family"
                + " members to register please enter the additional authorization"
                + " codes now. When you need a meal just text MEAL back to this number.", returnString);
        // addition auth code
        returnString = factory.handleEvent(new Event(SESSION_ID, PHONE_NUMBER, "111111"));
        Assert.assertEquals("We have added that authorization code to your family account. Please"
                + " send any additional codes you need for your family.", returnString);
        // addition auth code
        returnString = factory.handleEvent(new Event(SESSION_ID, PHONE_NUMBER, "222222"));
        Assert.assertEquals("We have added that authorization code to your family account. Please"
                + " send any additional codes you need for your family.", returnString);
    }
}
