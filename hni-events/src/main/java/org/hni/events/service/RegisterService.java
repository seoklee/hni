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
        boolean nextStep = true;
        switch (EventState.fromStateCode(state.getEventState().getStateCode())) {
            case STATE_REGISTER_START:
                user.setMobilePhone(event.getPhoneNumber());
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
                returnString = "Perfect! Lastly, I'd like to get your email address "
                        + "to verify your account in case you text me from a new "
                        + "number. So what's your email address? Thanks";
                // } else {
                // nextStep = false;
                // returnString = "We didn't get that. Please send your name again.";
                // }
                break;
            case STATE_REGISTER_GET_EMAIL:
                user.setEmail(textMessage);
                // validate the email
                // if (userService.validate(user)) {
                returnString = "Okay! I have " + textMessage + " as your email address. "
                        + "Is that correct? Reply 1 for yes and 2 for no";
                // } else {
                // nextStep = false;
                // returnString = "We didn't get that. Please send your email address.";
                // }
                break;
            case STATE_REGISTER_CONFIRM_EMAIL:
                if ("2".equals(textMessage)) {
                    user.setEmail(null);
                    nextStep = false;
                    returnString = "So what's your email address?";
                } else {
                    returnString = "Please enter the 6 digit authorization code provided to you for this program.";
                }
                break;
            case STATE_REGISTER_GET_AUTH_CODE:
                // if(userService.isAuthCodeValid(textMessage)) {
                returnString = "Ok. You're all setup for yourself.";
                // } else {
                // nextStep = false;
                // returnString = "The authorization code you entered (" + textMessage+") is not valid."
                // + " Please resend a valid unused authorization code";
                // }
                break;
            case STATE_REGISTER_COMPLETE:
                // save the complete user
                // userService.save(user);
                sessionStateDao.delete(event.getSessionId());
                return null;
            default:
                throw new RuntimeException("Unknown state");
        }
        if (nextStep) {
            final EventState nextStateCode = EventState.fromStateCode(state.getEventState().getStateCode() + 1);
            final SessionState nextState =
                    new SessionState(state.getEventName(), state.getSessionId(), state.getPhoneNumber(), serialize(user), nextStateCode);
            sessionStateDao.update(nextState);
        }
        return returnString;
    }

}
