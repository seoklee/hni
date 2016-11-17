package org.hni.events.service;

import org.hni.events.service.om.Event;
import org.hni.events.service.om.EventState;
import org.hni.events.service.om.SessionState;
import org.hni.user.om.User;
import org.hni.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class RegisterService extends AbstractEventService<User> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterService.class);

    @Inject
    private UserService userService;

    @Override
    public String handleEvent(final Event event) {
        final String returnString;

        final SessionState state = sessionStateDao.get(event.getSessionId());
        final User user = state.getPayload() != null ? deserialize(state.getPayload(), User.class) : new User();
        final String textMessage = event.getTextMessage();
        final EventState nextStateCode;
        switch (EventState.fromStateCode(state.getEventState().getStateCode())) {
            case STATE_REGISTER_START:
                user.setMobilePhone(event.getPhoneNumber());
                nextStateCode = EventState.STATE_REGISTER_GET_NAME;
                returnString = "Welcome to Hunger Not Impossible! Msg & data rates may apply. "
                        + "Any information you provide here will be kept private. "
                        + "Reply with PRIVACY to learn more. Let's get you registered. What's your name?";
                break;
            case STATE_REGISTER_GET_NAME:
                String[] name = textMessage.split(" ");
                user.setFirstName(name[0]);
                user.setLastName(name[1]);
                // validate the name
                // if (userService.validate(user)) {
                nextStateCode = EventState.STATE_REGISTER_GET_EMAIL;
                returnString = "Perfect! Lastly, I'd like to get your email address "
                        + "to verify your account in case you text me from a new "
                        + "number. So what's your email address? Thanks";
                // } else {
                // returnString = "We didn't get that. Please send your name again.";
                // }
                break;
            case STATE_REGISTER_GET_EMAIL:
                user.setEmail(textMessage);
                // validate the email
                // if (userService.validate(user)) {
                nextStateCode = EventState.STATE_REGISTER_CONFIRM_EMAIL;
                returnString = "Okay! I have " + textMessage + " as your email address. "
                        + "Is that correct? Reply 1 for yes and 2 for no";
                // } else {
                // returnString = "We didn't get that. Please send your email address.";
                // }
                break;
            case STATE_REGISTER_CONFIRM_EMAIL:
                if ("2".equals(textMessage)) {
                    user.setEmail(null);
                    nextStateCode = EventState.STATE_REGISTER_GET_EMAIL;
                    returnString = "So what's your email address?";
                } else {
                    nextStateCode = EventState.STATE_REGISTER_GET_AUTH_CODE;
                    returnString = "Please enter the 6 digit authorization code provided to you for this program.";
                }
                break;
            case STATE_REGISTER_GET_AUTH_CODE:
                // if(userService.isAuthCodeValid(textMessage)) {
                // save the complete user
                // userService.save(user);
                // link auth code with user
                nextStateCode = EventState.STATE_REGISTER_MORE_AUTH_CODES;
                returnString = "Ok. You're all setup for yourself. If you have additional family"
                        + " members to register please enter the additional authorization"
                        + " codes now. When you need a meal just text MEAL back to this number.";
                // } else {
                // returnString = "The authorization code you entered (" + textMessage + ") is not valid."
                // + " Please resend a valid unused authorization code";
                // }
                break;
            case STATE_REGISTER_MORE_AUTH_CODES:
                // if(userService.isAuthCodeValid(textMessage)) {
                // link auth code with user
                returnString = "We have added that authorization code to your family account. Please"
                        + " send any additional codes you need for your family.";
                // } else {
                // returnString = "The authorization code you entered (" + textMessage + ") is not valid."
                // + " Please resend a valid unused authorization code"
                // }
                // no longer need to change the state at this moment.
                return returnString;
            default:
                LOGGER.error("Unknown state {}", state.getEventState());
                return "Oops, an error occured. Please start over again.";
        }
        if (!state.getEventState().equals(nextStateCode)) {
            final SessionState nextState =
                    new SessionState(state.getEventName(), state.getSessionId(), state.getPhoneNumber(), serialize(user), nextStateCode);
            sessionStateDao.update(nextState);
        }
        return returnString;
    }

}
