package org.hni.events.service;

import org.hni.events.service.dao.RegistrationStateDAO;
import org.hni.events.service.om.Event;
import org.hni.events.service.om.RegistrationStep;
import org.hni.events.service.om.RegistrationState;
import org.hni.security.service.ActivationCodeService;
import org.hni.user.om.User;
import org.hni.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

@Component
public class RegisterService extends AbstractRegistrationService<User> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterService.class);

    public static String MSG_PRIVACY = "PRIVACY";
    public static String MSG_NONE = "NONE";
    
    public static String REPLY_WELCOME = "Welcome to Hunger Not Impossible! Msg & data rates may apply. "
            + "Information you provide will be kept private. "
            + "Reply with PRIVACY to learn more. Let's get you registered. What's your first name?";
    public static String REPLY_PRIVACY = "HNI respects your privacy and protects your data. "
            + "For more details on our privacy please visit http://hungernotimpossible.com/Privacy. "
            + "In order to continue the registration. ";

    public static String REPLY_NO_UNDERSTAND = "I didn't understand your last message. ";
    public static String REPLY_REQUEST_FIRST_NAME = "What's your first name?";
    public static String REPLY_REQUEST_LAST_NAME ="Thanks %s. What's your last name?";
    public static String REPLY_REQUEST_EMAIL = "Lastly, I'd like to get your email address "
            + "to verify your account in case you text me from a new number. Reply '%s' if you don't have an email.";

    public static String REPLY_EMAIL_NONE = "You don't have an email address. Is that correct? Reply 1 for yes and 2 for no.";
    public static String REPLY_EMAIL_CONFIRM = "I have %s as your email address. Is that correct? Reply 1 for yes and 2 for no.";
    public static String REPLY_EMAIL_INVALID = "I'm sorry that is not a valid email. Try again or type '%s' if don't have an email.";
    public static String REPLY_EMAIL_REQUEST = "Enter your email address.";
    
    public static String REPLY_AUTHCODE_REQUEST = "Please enter the 6 digit authorization code provided to you for this program.";
    public static String REPLY_AUTHCODE_INVALID = "The authorization code you entered (%s) is invalid. Please resend a valid unused authorization code.";
	public static String REPLY_AUTHCODE_ADDED = "I've added the authorization code to your family account.";
	public static String REPLY_AUTHCODE_ADDITIONAL = " If you have additional family members to register, enter the authorization codes now, one at a time.";
	
    public static String REPLY_REGISTRATION_COMPLETE = "Ok. You're all set up for yourself. When you need a meal just text MEAL back to this number. ";
    public static String REPLY_EXCEPTION_START_OVER = "Oops, an error occured. Please start over again.";
    
    @Inject
    private UserService customerService;

    @Inject
    private ActivationCodeService activationCodeService;

    @Inject
    private RegistrationStateDAO registrationStateDAO;

    @PostConstruct
    void init() {
        setRegistrationStateDAO(registrationStateDAO);
    }

    @Override
    protected String performWorkFlowStep(final Event event, final RegistrationState registrationState) {
        final String returnString;
        final User user =
                registrationState.getPayload() != null ? deserialize(registrationState.getPayload(), User.class) : new User();
        final String textMessage = event.getTextMessage();
        switch (RegistrationStep.fromStateCode(registrationState.getRegistrationStep().getStateCode())) {
            case STATE_REGISTER_START:
                user.setMobilePhone(event.getPhoneNumber());
                registrationState.setRegistrationStep(RegistrationStep.STATE_REGISTER_GET_FIRST_NAME);
                returnString = REPLY_WELCOME;
                break;
            case STATE_REGISTER_GET_FIRST_NAME:
                if (!textMessage.equalsIgnoreCase(MSG_PRIVACY)) {
                    user.setFirstName(textMessage);
                    // validate the first name
                    if (customerService.validate(user)) {
                        registrationState.setRegistrationStep(RegistrationStep.STATE_REGISTER_GET_LAST_NAME);
                        returnString = String.format(REPLY_REQUEST_LAST_NAME, textMessage);
                    } else {
                        returnString = REPLY_NO_UNDERSTAND + REPLY_REQUEST_FIRST_NAME;
                    }
                } else {
                    returnString = REPLY_PRIVACY + REPLY_REQUEST_FIRST_NAME;
                }
                break;
            case STATE_REGISTER_GET_LAST_NAME:
                user.setLastName(textMessage);
                // validate the last name
                if (customerService.validate(user)) {
                    registrationState.setRegistrationStep(RegistrationStep.STATE_REGISTER_GET_EMAIL);
                    returnString = String.format(REPLY_REQUEST_EMAIL, MSG_NONE);
                } else {
                    returnString = REPLY_NO_UNDERSTAND + REPLY_REQUEST_LAST_NAME;
                }
                break;
            case STATE_REGISTER_GET_EMAIL:
                user.setEmail(textMessage);
                // validate the email
                if (customerService.validate(user)) {
                    registrationState.setRegistrationStep(RegistrationStep.STATE_REGISTER_CONFIRM_EMAIL);
                    if ("none".equalsIgnoreCase(textMessage)) {
                        returnString = REPLY_EMAIL_NONE;
                    } else {
                        returnString = String.format(REPLY_EMAIL_CONFIRM, user.getEmail());
                    }
                } else {
                    returnString = String.format(REPLY_EMAIL_INVALID, MSG_NONE);
                }
                break;
            case STATE_REGISTER_CONFIRM_EMAIL:
                switch (textMessage){
            		case "2":
            			user.setEmail(null);
                        registrationState.setRegistrationStep(RegistrationStep.STATE_REGISTER_GET_EMAIL);
                        returnString = REPLY_EMAIL_REQUEST;
                        break;
            		case "1":
                        registrationState.setRegistrationStep(RegistrationStep.STATE_REGISTER_GET_AUTH_CODE);
                        returnString = REPLY_AUTHCODE_REQUEST;
                        break;
            		default:
                        registrationState.setRegistrationStep(RegistrationStep.STATE_REGISTER_CONFIRM_EMAIL);
            			returnString = String.format(REPLY_EMAIL_CONFIRM, user.getEmail());
            			break;
            	}
                break;
            case STATE_REGISTER_GET_AUTH_CODE:
                if (activationCodeService.validate(textMessage)) {
                    customerService.save(user);
                    //we are sure that text message is a long at this point
                    customerService.registerCustomer(user, textMessage);
                    registrationState.setRegistrationStep(RegistrationStep.STATE_REGISTER_MORE_AUTH_CODES);
                    returnString = REPLY_REGISTRATION_COMPLETE + REPLY_AUTHCODE_ADDITIONAL;
                } else {
                    returnString = String.format(REPLY_AUTHCODE_INVALID, textMessage); 
                }
                break;
            case STATE_REGISTER_MORE_AUTH_CODES:
                if (activationCodeService.validate(textMessage)) {
                    customerService.registerCustomer(user, textMessage);
                    // link auth code with user
                    returnString = REPLY_AUTHCODE_ADDED + REPLY_AUTHCODE_ADDITIONAL;
                } else {
                    returnString = String.format(REPLY_AUTHCODE_INVALID, textMessage);
                }
                // no longer need to change the state at this moment.
                break;
            default:
                LOGGER.error("Unknown state {}", registrationState.getRegistrationStep());
                returnString = REPLY_EXCEPTION_START_OVER;
                break;
        }
        registrationState.setPayload(serialize((user)));
        return returnString;
    }
}
