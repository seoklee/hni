package org.hni.events.service;

import org.hni.events.service.dao.EventStateDao;
import org.hni.events.service.dao.RegistrationStateDAO;
import org.hni.events.service.om.Event;
import org.hni.events.service.om.EventName;
import org.hni.events.service.om.EventState;
import org.hni.events.service.om.RegistrationState;
import org.hni.events.service.om.RegistrationStep;
import org.hni.security.service.ActivationCodeService;
import org.hni.user.om.User;
import org.hni.user.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.transaction.Transactional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:test-applicationContext.xml"} )
@Transactional
public class EventRouterIntTest {

    private static final String MEDIA_TYPE = "text/plain";
    private static final String PHONE_NUMBER = "8188461238";
    private static final String FIRST_NAME =  "john";
    private static final String LAST_NAME = "doe";
    private static final String EMAIL = "johndoe@gmail.com";
    private static final String AUTH_CODE_1 = "123123";
    private static final String AUTH_CODE_2 = "987654";


    @Spy
    @InjectMocks
    private RegisterService registerService = new RegisterService();

    @Spy
    @Inject
    private RegistrationStateDAO registrationStateDAO;

    @Spy
    @Inject
    private EventStateDao eventStateDao;

    @InjectMocks
    private EventRouter factory = new EventRouter();

    @Mock
    private UserService customerService;

    @Mock
    private ActivationCodeService activationCodeService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        factory.init();
        when(customerService.validate(any(User.class))).thenReturn(true);
        when(activationCodeService.validate(eq(AUTH_CODE_1))).thenReturn(true);
        when(activationCodeService.validate(eq(AUTH_CODE_2))).thenReturn(true);
    }

    @Test
    public void testStartRegister() throws Exception {
        String returnString = factory.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, "REGISTER"));
        assertEquals("Welcome to Hunger Not Impossible! Msg & data rates may apply. "
            + "Information you provide will be kept private. Reply with PRIVACY to learn more. "
            + "Let's get you registered. What's your first name?", returnString);
        verify(registrationStateDAO, times(1)).insert(any(RegistrationState.class));
        RegistrationState nextState = registrationStateDAO.getByPhoneNumber(PHONE_NUMBER);
        assertEquals(PHONE_NUMBER, nextState.getPhoneNumber());
        assertEquals(EventName.REGISTER, nextState.getEventName());
        assertEquals(RegistrationStep.STATE_REGISTER_GET_FIRST_NAME, nextState.getRegistrationStep());
    }

    @Test
    public void testInterruptExistingWorkFlow() {

        //stubbing for MEAL EventState
        when(eventStateDao.byPhoneNumber(PHONE_NUMBER))
            .thenReturn(new EventState(EventName.MEAL, PHONE_NUMBER));

        //stubbing for MORE AUTH CODES response, because erase @ registrationStateDAO does not happen.
        when(registrationStateDAO.getByPhoneNumber(PHONE_NUMBER))
            .thenReturn(RegistrationState.create(
                EventName.MEAL,
                PHONE_NUMBER,
                null,
                RegistrationStep.STATE_REGISTER_MORE_AUTH_CODES));

        //TODO Fix this with a message that makes sense during interruption refactor
        String returnString = factory.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, "REGISTER"));
        assertEquals("The authorization code you entered (REGISTER) is invalid. "
                + "Please resend a valid unused authorization code.", returnString);
    }

    @Test
    public void testRegisterWorkFlow() throws Exception {
        // enroll keyword to start register workflow

        String returnString = factory.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, "ENROLL"));
        assertEquals("Welcome to Hunger Not Impossible! Msg & data rates may apply. "
            + "Information you provide will be kept private. Reply with PRIVACY to learn more. "
            + "Let's get you registered. What's your first name?", returnString);
        // first name
        returnString = factory.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, FIRST_NAME));
        assertEquals("Thanks " + "john" + ". What's your last name?", returnString);
        // last name
        returnString = factory.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, LAST_NAME));
        assertEquals("Lastly, I'd like to get your email address to verify your account "
            + "in case you text me from a new number. Reply 'NONE' if you don't have an email.", returnString);
        // email
        returnString = factory.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, EMAIL));
        assertEquals("I have johndoe@gmail.com as your email address. Is that correct? "
            + "Reply 1 for yes and 2 for no.", returnString);
        // confirm email
        returnString = factory.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, "1"));
        assertEquals("Please enter the 6 digit authorization code provided to you for this program.", returnString);
        // auth code
        returnString = factory.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, AUTH_CODE_1));
        assertEquals("Ok. You're all set up for yourself. "
            + "When you need a meal just text MEAL back to this number.  "
            + "If you have additional family members to register, "
            + "enter the authorization codes now, one at a time.", returnString);
        // addition auth code
        returnString = factory.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, AUTH_CODE_2));
        assertEquals("I've added the authorization code to your family account. "
            + "If you have additional family members to register, "
            + "enter the authorization codes now, one at a time.", returnString);

        RegistrationState regState = registrationStateDAO.getByPhoneNumber(PHONE_NUMBER);
        assertEquals(regState.getEventName(), EventName.REGISTER);
        assertEquals(regState.getPhoneNumber(), PHONE_NUMBER);
        assertEquals(regState.getRegistrationStep(), RegistrationStep.STATE_REGISTER_MORE_AUTH_CODES);
    }
}
