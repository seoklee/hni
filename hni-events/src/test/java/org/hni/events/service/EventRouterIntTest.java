package org.hni.events.service;

import org.hni.events.service.dao.DefaultRegistrationStateDAO;
import org.hni.events.service.dao.RegistrationStateDAO;
import org.hni.events.service.om.Event;
import org.hni.events.service.om.EventName;
import org.hni.events.service.om.RegistrationState;
import org.hni.events.service.om.RegistrationStep;
import org.hni.security.service.ActivationCodeService;
import org.hni.user.om.User;
import org.hni.user.service.UserService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

// TODO: Fix this integration test later
@Ignore
public class EventRouterIntTest {

    private static final String MEDIA_TYPE = "text/plain";
    private static final String PHONE_NUMBER = "8188461238";
    private static final String AUTH_CODE = "123456";

    @InjectMocks
    private EventRouter factory;

    @Spy
    @InjectMocks
    private RegisterService registerService = new RegisterService();

    @Spy
    private RegistrationStateDAO registrationStateDao = new DefaultRegistrationStateDAO();

    @Mock
    private UserService customerService;

    @Mock
    private ActivationCodeService activationCodeService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        factory.init();
        when(customerService.validate(any(User.class))).thenReturn(true);
        when(activationCodeService.validate(eq(AUTH_CODE))).thenReturn(true);
    }

    @Test
    public void testStartRegister() throws Exception {
        String returnString = factory.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, "REGISTER"));
        Assert.assertEquals("Welcome to Hunger Not Impossible! Msg & data rates may apply. "
                + "Any information you provide here will be kept private. "
                + "Reply with PRIVACY to learn more. Let's get you registered. What's your first name?", returnString);
        verify(registrationStateDao, times(1)).insert(any(RegistrationState.class));
        verify(registrationStateDao, times(1)).update(any(RegistrationState.class));
        RegistrationState nextState = registrationStateDao.getByPhoneNumber(PHONE_NUMBER);
        Assert.assertEquals(PHONE_NUMBER, nextState.getPhoneNumber());
        Assert.assertEquals(EventName.REGISTER, nextState.getEventName());
        Assert.assertEquals(RegistrationStep.STATE_REGISTER_GET_FIRST_NAME, nextState.getRegistrationStep());
    }

    @Test
    public void testInterruptExistingWorkFlow() {
        registrationStateDao.insert(new RegistrationState( null, EventName.MEAL, PHONE_NUMBER, null, null));
        String returnString = factory.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, "REGISTER"));
        Assert.assertEquals("Welcome to Hunger Not Impossible! Msg & data rates may apply. "
                + "Any information you provide here will be kept private. "
                + "Reply with PRIVACY to learn more. Let's get you registered. What's your first name?", returnString);
        verify(registrationStateDao, times(1)).deleteByPhoneNumber(eq(PHONE_NUMBER));
        verify(registrationStateDao, times(2)).insert(any(RegistrationState.class));
    }

    @Test
    public void testRegisterWorkFlow() throws Exception {
        // enroll keyword to start register workflow
        String returnString = factory.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, "ENROLL"));
        Assert.assertEquals("Welcome to Hunger Not Impossible! Msg & data rates may apply. "
                + "Any information you provide here will be kept private. "
                + "Reply with PRIVACY to learn more. Let's get you registered. What's your first name?", returnString);
        // first name
        returnString = factory.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, "john"));
        Assert.assertEquals("Thanks " + "john" + ". What's your last name?", returnString);
        // last name
        returnString = factory.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, "doe"));
        Assert.assertEquals("Perfect! Lastly, I'd like to get your email address "
                + "to verify your account in case you text me from a new "
                + "number. So what's your email address? Type 'none' if you don't have an email. Thanks", returnString);
        // email
        returnString = factory.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, "johndoe@gmail.com"));
        Assert.assertEquals("Okay! I have " + "johndoe@gmail.com" + " as your email address. "
                + "Is that correct? Reply 1 for yes and 2 for no", returnString);
        // confirm email
        returnString = factory.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, "1"));
        Assert.assertEquals("Please enter the 6 digit authorization code provided to you for this program.", returnString);
        // auth code
        returnString = factory.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, "123456"));
        Assert.assertEquals("Ok. You're all setup for yourself. If you have additional family"
                + " members to register please enter the additional authorization"
                + " codes now. When you need a meal just text MEAL back to this number.", returnString);
        // addition auth code
        returnString = factory.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, "111111"));
        Assert.assertEquals("The authorization code you entered (" + "111111" + ") is not valid."
                + " Please resend a valid unused authorization code", returnString);
    }
}
