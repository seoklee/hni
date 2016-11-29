package org.hni.events.service;

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
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

public class RegisterServiceUnitTest {

    private static final String PHONE_NUMBER = "8188461238";
    private static final String AUTH_CODE = "123456";
    private static final Long REGISTRATION_STATE_ID = 1L;

    @InjectMocks
    private RegisterService registerService;

    @Mock
    private RegistrationStateDAO registrationStateDAO;

    @Mock
    private UserService customerService;

    @Mock
    private ActivationCodeService activationCodeService;

    private Event event;
    private RegistrationState state = null;
    private String payload = null;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        event = Event.createEvent("text/plain", PHONE_NUMBER, "message");
        when(customerService.validate(any(User.class))).thenReturn(true);
        when(activationCodeService.validate(eq(AUTH_CODE))).thenReturn(true);
    }

    @Test
    public void testStartRegister() throws Exception {
        state = new RegistrationState(null, EventName.REGISTER, PHONE_NUMBER, payload, RegistrationStep.STATE_REGISTER_START);
        when(registrationStateDAO.getByPhoneNumber(eq(PHONE_NUMBER))).thenReturn(state);

        String returnString = registerService.handleEvent(event);
        Assert.assertEquals("Welcome to Hunger Not Impossible! Msg & data rates may apply. "
                + "Information you provide will be kept private. "
                + "Reply with PRIVACY to learn more. Let's get you registered. What's your first name?", returnString);
        verify(registrationStateDAO, times(1)).update(any(RegistrationState.class));
    }

    @Test
    public void testPrivacy() throws Exception {
        state = new RegistrationState(EventName.REGISTER, PHONE_NUMBER, payload, RegistrationStep.STATE_REGISTER_GET_FIRST_NAME);
        when(registrationStateDAO.get(eq(PHONE_NUMBER))).thenReturn(state);
        event.setTextMessage("privacy");
        String returnString = registerService.handleEvent(event);
        Assert.assertEquals("HNI respects your privacy and protects your data. "
                        + "For more details on our privacy please visit http://hungernotimpossible.com/Privacy. "
                        + "In order to continue the registration. Please send us your first name.", returnString);
        verify(registrationStateDAO, never()).update(any(RegistrationState.class));
    }

    @Test
    public void testGetFirstName() throws Exception {
        state = new RegistrationState(REGISTRATION_STATE_ID, EventName.REGISTER, PHONE_NUMBER, payload, RegistrationStep.STATE_REGISTER_GET_FIRST_NAME);
        event.setTextMessage("firstname");

        when(registrationStateDAO.getByPhoneNumber(eq(PHONE_NUMBER))).thenReturn(state);
        String returnString = registerService.handleEvent(event);
        Assert.assertEquals("Thanks " + "firstname" + ". What's your last name?", returnString);
        verify(registrationStateDAO, times(1)).update(any(RegistrationState.class));
    }

    @Test
    public void testGetLastName() throws Exception {
        state = new RegistrationState(REGISTRATION_STATE_ID, EventName.REGISTER, PHONE_NUMBER, payload, RegistrationStep.STATE_REGISTER_GET_LAST_NAME);
        event.setTextMessage("lastname");

        when(registrationStateDAO.getByPhoneNumber(eq(PHONE_NUMBER))).thenReturn(state);
        String returnString = registerService.handleEvent(event);
        Assert.assertEquals("Lastly, I'd like to get your email address "
                + "to verify your account in case you text me from a new "
                + "number. Type 'none' if you don't have an email.", returnString);
        verify(registrationStateDAO, times(1)).update(any(RegistrationState.class));
    }

    @Test
    public void testGetEmail() throws Exception {
        state = new RegistrationState(REGISTRATION_STATE_ID, EventName.REGISTER, PHONE_NUMBER, payload, RegistrationStep.STATE_REGISTER_GET_EMAIL);
        event.setTextMessage("johndoe@gmail.com");

        when(registrationStateDAO.getByPhoneNumber(eq(PHONE_NUMBER))).thenReturn(state);
        String returnString = registerService.handleEvent(event);
        Assert.assertEquals("Okay! I have " + "johndoe@gmail.com" + " as your email address. "
                + "Is that correct? Reply 1 for yes and 2 for no", returnString);
        verify(registrationStateDAO, times(1)).update(any(RegistrationState.class));
    }

    @Test
    public void testGetNoneEmail() throws Exception {
        state = new RegistrationState(REGISTRATION_STATE_ID, EventName.REGISTER, PHONE_NUMBER, payload, RegistrationStep.STATE_REGISTER_GET_EMAIL);
        event.setTextMessage("none");

        when(registrationStateDAO.getByPhoneNumber(eq(PHONE_NUMBER))).thenReturn(state);
        String returnString = registerService.handleEvent(event);
        Assert.assertEquals("Okay! You don't have an email address. "
                + "Is that correct? Reply 1 for yes and 2 for no", returnString);
        verify(registrationStateDAO, times(1)).update(any(RegistrationState.class));
    }

    @Test
    public void testConfirmEmail() throws Exception {
        state = new RegistrationState(REGISTRATION_STATE_ID, EventName.REGISTER, PHONE_NUMBER, payload, RegistrationStep.STATE_REGISTER_CONFIRM_EMAIL);
        event.setTextMessage("1");

        when(registrationStateDAO.getByPhoneNumber(eq(PHONE_NUMBER))).thenReturn(state);
        String returnString = registerService.handleEvent(event);
        Assert.assertEquals("Please enter the 6 digit authorization code provided to you for this program.", returnString);
        verify(registrationStateDAO, times(1)).update(any(RegistrationState.class));
    }

    @Test
    public void testGetAuthCode() throws Exception {
        state = new RegistrationState(REGISTRATION_STATE_ID, EventName.REGISTER, PHONE_NUMBER, payload, RegistrationStep.STATE_REGISTER_GET_AUTH_CODE);
        event.setTextMessage(AUTH_CODE);

        when(registrationStateDAO.getByPhoneNumber(eq(PHONE_NUMBER))).thenReturn(state);
        String returnString = registerService.handleEvent(event);
        System.out.println(returnString);
        Assert.assertEquals("Ok. You're all set up for yourself. If you have family"
                + " members to register please enter the additional authorization"
                + " codes now, one at a time. When you need a meal just text MEAL back to this number.", returnString);
        verify(registrationStateDAO, times(1)).update(any(RegistrationState.class));
    }

    @Test
    public void testAddMoreAuthCodes() throws Exception {
        state = new RegistrationState(REGISTRATION_STATE_ID, EventName.REGISTER, PHONE_NUMBER, payload, RegistrationStep.STATE_REGISTER_MORE_AUTH_CODES);
        event.setTextMessage(AUTH_CODE);

        when(registrationStateDAO.getByPhoneNumber(eq(PHONE_NUMBER))).thenReturn(state);
        String returnString = registerService.handleEvent(event);
        Assert.assertEquals("We have added that authorization code to your family account. Please"
                + " send any additional codes you need for your family.", returnString);
        verify(registrationStateDAO, never()).update(any(RegistrationState.class));
    }
}
