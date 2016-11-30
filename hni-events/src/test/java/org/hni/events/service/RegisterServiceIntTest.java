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

import javax.ws.rs.HEAD;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@Ignore
public class RegisterServiceIntTest {

    //TODO FIX SESSION_ID and phoneNumber REFACTOR
    private static final String MEDIA_TYPE = "text/plain";
    private static final String SESSION_ID = "123";
    private static final String PHONE_NUMBER = "8188461238";
    private static final String AUTH_CODE = "123456";

    @InjectMocks
    private RegisterService registerService;

    @Spy
    RegistrationStateDAO registrationStateDAO = new DefaultRegistrationStateDAO();

    @Mock
    private UserService customerService;

    @Mock
    private ActivationCodeService activationCodeService;

    private RegistrationState state;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        state = RegistrationState.create(EventName.REGISTER, PHONE_NUMBER, null, RegistrationStep.STATE_REGISTER_START);
        registrationStateDAO.insert(state);
        when(customerService.validate(any(User.class))).thenReturn(true);
        when(activationCodeService.validate(eq(AUTH_CODE))).thenReturn(true);
    }

    @Test
    public void testStartRegister() throws Exception {
        String returnString = registerService.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, "message"));
        Assert.assertEquals("Welcome to Hunger Not Impossible! Msg & data rates may apply. "
                + "Information you provide will be kept private. "
                + "Reply with PRIVACY to learn more. Let's get you registered. What's your first name?", returnString);

        RegistrationState nextState = registrationStateDAO.getByPhoneNumber(PHONE_NUMBER);

        Assert.assertEquals(PHONE_NUMBER, nextState.getPhoneNumber());
        Assert.assertEquals(EventName.REGISTER, nextState.getEventName());
        Assert.assertEquals(RegistrationStep.STATE_REGISTER_GET_FIRST_NAME, nextState.getRegistrationStep());
    }

    @Test
    public void testRegisterWorkFlow() throws Exception {
        // enroll keyword to start register workflow
        String returnString = registerService.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, "ENROLL"));
        Assert.assertEquals("Welcome to Hunger Not Impossible! Msg & data rates may apply. "
                + "Information you provide will be kept private. "
                + "Reply with PRIVACY to learn more. Let's get you registered. What's your first name?", returnString);
        // first name
        returnString = registerService.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, "john"));
        Assert.assertEquals("Thanks " + "john" + ". What's your last name?", returnString);
        // last name
        
        returnString = "Lastly, I'd like to get your email address "
                + "to verify your account in case you text me from a new "
                + "number. Type 'none' if you "
                + "don't have an email.";
        
        returnString = registerService.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, "doe"));
        Assert.assertEquals("Lastly, I'd like to get your email address "
                + "to verify your account in case you text me from a new "
                + "number. Type 'none' if you don't have an email.", returnString);
        // email
        returnString = registerService.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, "johndoe@gmail.com"));
        Assert.assertEquals("Okay! I have " + "johndoe@gmail.com" + " as your email address. "
                + "Is that correct? Reply 1 for yes and 2 for no", returnString);
        // confirm email
        returnString = registerService.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, "1"));
        Assert.assertEquals("Please enter the 6 digit authorization code provided to you for this program.", returnString);
        // auth code
        returnString = registerService.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, "123456"));
        Assert.assertEquals("Ok. You're all setup for yourself. If you have family"
                + " members to register please enter the additional authorization"
                + " codes now. When you need a meal just text MEAL back to this number.", returnString);
        // addition auth code
        when(activationCodeService.validate(eq("111111"))).thenReturn(true);
        returnString = registerService.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, "111111"));
        Assert.assertEquals("We have added that authorization code to your family account. Please"
                + " send any additional codes you need for your family.", returnString);
    }

    @Test
    public void testRegisterWithPrivacy() throws Exception {
        // enroll keyword to start register workflow
        String returnString = registerService.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, "ENROLL"));
        Assert.assertEquals("Welcome to Hunger Not Impossible! Msg & data rates may apply. "
                + "Information you provide will be kept private. "
                + "Reply with PRIVACY to learn more. Let's get you registered. What's your first name?", returnString);
        //privacy
        returnString = registerService.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, "privacy"));
        Assert.assertEquals("HNI respects your privacy and protects your data. "
                        + "For more details on our privacy please visit http://hungernotimpossible.com/Privacy. "
                        + "In order to continue the registration. Please send us your first name.", returnString);
        // first name
        returnString = registerService.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, "john"));
        Assert.assertEquals("Thanks " + "john" + ". What's your last name?", returnString);
        // last name
        returnString = registerService.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, "doe"));
        Assert.assertEquals("Lastly, I'd like to get your email address "
                + "to verify your account in case you text me from a new "
                + "number. Type 'none' if you don't have an email.", returnString);
        // email
        returnString = registerService.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, "johndoe@gmail.com"));
        Assert.assertEquals("Okay! I have " + "johndoe@gmail.com" + " as your email address. "
                + "Is that correct? Reply 1 for yes and 2 for no", returnString);
        // confirm email
        returnString = registerService.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, "1"));
        Assert.assertEquals("Please enter the 6 digit authorization code provided to you for this program.", returnString);
        // auth code
        returnString = registerService.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, "123456"));
        Assert.assertEquals("Ok. You're all setup for yourself. If you have family"
                + " members to register please enter the additional authorization"
                + " codes now. When you need a meal just text MEAL back to this number.", returnString);
        // addition auth code
        when(activationCodeService.validate(eq("111111"))).thenReturn(true);
        returnString = registerService.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, "111111"));
        Assert.assertEquals("We have added that authorization code to your family account. Please"
                + " send any additional codes you need for your family.", returnString);
    }

    @Test
    public void testCorrectEmailOnce() throws Exception {
        // enroll keyword to start register workflow
        String returnString = registerService.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, "ENROLL"));
        Assert.assertEquals("Welcome to Hunger Not Impossible! Msg & data rates may apply. "
                + "Information you provide will be kept private. "
                + "Reply with PRIVACY to learn more. Let's get you registered. What's your first name?", returnString);
        // first name
        returnString = registerService.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, "john"));
        Assert.assertEquals("Thanks " + "john" + ". What's your last name?", returnString);
        // last name
        returnString = registerService.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, "doe"));
        Assert.assertEquals("Lastly, I'd like to get your email address "
                + "to verify your account in case you text me from a new "
                + "number. Type 'none' if you don't have an email.", returnString);
        // wrong email
        returnString = registerService.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, "johndoe@gmail.com"));
        Assert.assertEquals("Okay! I have " + "johndoe@gmail.com" + " as your email address. "
                + "Is that correct? Reply 1 for yes and 2 for no", returnString);
        // reject email
        returnString = registerService.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, "2"));
        Assert.assertEquals("So what's your email address?", returnString);
        // no email
        returnString = registerService.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, "none"));
        Assert.assertEquals("Okay! You don't have an email address. "
                + "Is that correct? Reply 1 for yes and 2 for no", returnString);
        // confirm email
        returnString = registerService.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, "1"));
        Assert.assertEquals("Please enter the 6 digit authorization code provided to you for this program.", returnString);
        // auth code
        returnString = registerService.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, "123456"));
        Assert.assertEquals("Ok. You're all setup for yourself. If you have family"
                + " members to register please enter the additional authorization"
                + " codes now. When you need a meal just text MEAL back to this number.", returnString);
    }

    @Test
    public void testInvalidAdditionalAuthCode() throws Exception {
        // enroll keyword to start register workflow
        String returnString = registerService.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, "ENROLL"));
        Assert.assertEquals("Welcome to Hunger Not Impossible! Msg & data rates may apply. "
                + "Information you provide will be kept private. "
                + "Reply with PRIVACY to learn more. Let's get you registered. What's your first name?", returnString);
        // first name
        returnString = registerService.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, "john"));
        Assert.assertEquals("Thanks " + "john" + ". What's your last name?", returnString);
        // last name
        returnString = registerService.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, "doe"));
        Assert.assertEquals("Lastly, I'd like to get your email address "
                + "to verify your account in case you text me from a new "
                + "number. Type 'none' if you don't have an email.", returnString);
        // email
        returnString = registerService.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, "johndoe@gmail.com"));
        Assert.assertEquals("Okay! I have " + "johndoe@gmail.com" + " as your email address. "
                + "Is that correct? Reply 1 for yes and 2 for no", returnString);
        // confirm email
        returnString = registerService.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, "1"));
        Assert.assertEquals("Please enter the 6 digit authorization code provided to you for this program.", returnString);
        // auth code
        returnString = registerService.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, "123456"));
        Assert.assertEquals("Ok. You're all setup for yourself. If you have family"
                + " members to register please enter the additional authorization"
                + " codes now. When you need a meal just text MEAL back to this number.", returnString);
        // addition auth code
        returnString = registerService.handleEvent(Event.createEvent(MEDIA_TYPE, PHONE_NUMBER, "111111"));
        Assert.assertEquals("The authorization code you entered (" + "111111" + ") is not valid."
                + " Please resend a valid unused authorization code", returnString);
    }

}
