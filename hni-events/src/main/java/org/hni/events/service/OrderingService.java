package org.hni.events.service;

import org.hni.events.service.om.Event;
import org.hni.events.service.om.EventState;
import org.hni.events.service.om.SessionState;
import org.hni.order.om.Order;
import org.hni.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class OrderingService extends AbstractEventService<Order> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderingService.class);

    @Inject
    private OrderService orderService;

    @Override
    protected WorkFlowStepResult performWorkFlowStep(final Event event, final SessionState sessionState) {
        final String returnString;
        EventState nextStateCode = sessionState.getEventState();
        final Order order =
                sessionState.getPayload() != null ? deserialize(sessionState.getPayload(), Order.class) : new Order();
        final String textMessage = event.getTextMessage();
        switch (EventState.fromStateCode(sessionState.getEventState().getStateCode())) {
            case STATE_MEAL_START:
                nextStateCode = EventState.STATE_MEAL_GET_ADDRESS;
                returnString = "Please send me your address and "
                        + "city so I can find the closest meals to you. For example: 111 S Main St, Bentonville";
                break;
            case STATE_MEAL_GET_ADDRESS:
                nextStateCode = EventState.STATE_MEAL_GET_LOCATION;
                returnString = "OK! Here's what I found. Reply with the number for the location /meal you prefer"
                        + " (for example: 2) or # to start over.";
                break;
            default:
                LOGGER.error("Unknown state {}", sessionState.getEventState());
                returnString = "Oops, an error occured. Please start over again.";
                break;
        }
        return new WorkFlowStepResult(returnString, nextStateCode, serialize(order));
    }

}
