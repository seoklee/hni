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
        state = RegistrationState.create(EventName.REGISTER, PHONE_NUMBER, payload, RegistrationStep.STATE_REGISTER_START);
        when(registrationStateDAO.getByPhoneNumber(eq(PHONE_NUMBER))).thenReturn(state);

        String returnString = registerService.handleEvent(event);
        Assert.assertEquals(RegisterService.REPLY_WELCOME, returnString);
    }

    @Test
    public void testPrivacy() throws Exception {
        state = RegistrationState.create(EventName.REGISTER, PHONE_NUMBER, payload, RegistrationStep.STATE_REGISTER_GET_FIRST_NAME);
        when(registrationStateDAO.getByPhoneNumber(eq(PHONE_NUMBER))).thenReturn(state);
        event.setTextMessage("privacy");
        String returnString = registerService.handleEvent(event);
        Assert.assertEquals(RegisterService.REPLY_PRIVACY + RegisterService.REPLY_REQUEST_FIRST_NAME, returnString);
        verify(registrationStateDAO, never()).update(any(RegistrationState.class));
    }

    @Test
    public void testGetFirstName() throws Exception {
        state = RegistrationState.create(EventName.REGISTER, PHONE_NUMBER, payload, RegistrationStep.STATE_REGISTER_GET_FIRST_NAME);
        event.setTextMessage("firstname");

        when(registrationStateDAO.getByPhoneNumber(eq(PHONE_NUMBER))).thenReturn(state);
        String returnString = registerService.handleEvent(event);
        Assert.assertEquals(String.format(RegisterService.REPLY_REQUEST_LAST_NAME, "firstname"), returnString);
    }

    @Test
    public void testGetLastName() throws Exception {
        state = RegistrationState.create(EventName.REGISTER, PHONE_NUMBER, payload, RegistrationStep.STATE_REGISTER_GET_LAST_NAME);
        event.setTextMessage("lastname");

        when(registrationStateDAO.getByPhoneNumber(eq(PHONE_NUMBER))).thenReturn(state);
        String returnString = registerService.handleEvent(event);
        Assert.assertEquals(String.format(RegisterService.REPLY_REQUEST_EMAIL, RegisterService.MSG_NONE), returnString);
    }

    @Test
    public void testGetEmail() throws Exception {
        state = RegistrationState.create(EventName.REGISTER, PHONE_NUMBER, payload, RegistrationStep.STATE_REGISTER_GET_EMAIL);
        event.setTextMessage("johndoe@gmail.com");

        when(registrationStateDAO.getByPhoneNumber(eq(PHONE_NUMBER))).thenReturn(state);
        String returnString = registerService.handleEvent(event);
        Assert.assertEquals(String.format(RegisterService.REPLY_EMAIL_CONFIRM, "johndoe@gmail.com"), returnString);
    }

    @Test
    public void testGetNoneEmail() throws Exception {
        state = RegistrationState.create(EventName.REGISTER, PHONE_NUMBER, payload, RegistrationStep.STATE_REGISTER_GET_EMAIL);
        event.setTextMessage("none");

        when(registrationStateDAO.getByPhoneNumber(eq(PHONE_NUMBER))).thenReturn(state);
        String returnString = registerService.handleEvent(event);
        Assert.assertEquals(RegisterService.REPLY_EMAIL_NONE, returnString);
    }

    @Test
    public void testConfirmEmail() throws Exception {
        state = RegistrationState.create(EventName.REGISTER, PHONE_NUMBER, payload, RegistrationStep.STATE_REGISTER_CONFIRM_EMAIL);
        event.setTextMessage("1");

        when(registrationStateDAO.getByPhoneNumber(eq(PHONE_NUMBER))).thenReturn(state);
        String returnString = registerService.handleEvent(event);
        Assert.assertEquals(RegisterService.REPLY_AUTHCODE_REQUEST, returnString);
    }

    @Test
    public void testGetAuthCode() throws Exception {
        state = RegistrationState.create(EventName.REGISTER, PHONE_NUMBER, payload, RegistrationStep.STATE_REGISTER_GET_AUTH_CODE);
        event.setTextMessage(AUTH_CODE);

        when(registrationStateDAO.getByPhoneNumber(eq(PHONE_NUMBER))).thenReturn(state);
        String returnString = registerService.handleEvent(event);
        System.out.println(returnString);
        Assert.assertEquals(RegisterService.REPLY_REGISTRATION_COMPLETE + RegisterService.REPLY_AUTHCODE_ADDITIONAL, returnString);
    }

    @Test
    public void testAddMoreAuthCodes() throws Exception {
        state = RegistrationState.create(EventName.REGISTER, PHONE_NUMBER, payload, RegistrationStep.STATE_REGISTER_MORE_AUTH_CODES);
        event.setTextMessage(AUTH_CODE);

        when(registrationStateDAO.getByPhoneNumber(eq(PHONE_NUMBER))).thenReturn(state);
        String returnString = registerService.handleEvent(event);
        Assert.assertEquals(RegisterService.REPLY_AUTHCODE_ADDED + RegisterService.REPLY_AUTHCODE_ADDITIONAL, returnString);
    }
}
