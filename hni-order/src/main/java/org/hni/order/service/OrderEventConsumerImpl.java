package org.hni.order.service;

import org.hni.order.om.Order;
import org.hni.order.om.OrderItem;
import org.hni.order.om.rs.OrderConfirmedMessage;
import org.hni.order.service.rs.client.SlackWebHookClient;
import org.redisson.api.RRemoteService;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * This implementation of OrderEventConsumer registers itself as a Redission remote service.
 */
@Component
public class OrderEventConsumerImpl implements OrderEventConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderEventConsumerImpl.class);

    private static final int CONCURRENT_INVOCATION_NUMBER = 1;

    @Value("#{hniProperties['hni.homepage']}")
    private String hniHomepage;
    @Inject
    private LockingService<RedissonClient> redissonClient;
    @Inject
    private SlackWebHookClient slackWebHookClient;

    @PostConstruct
    private void register() {
        RRemoteService remoteService = redissonClient.getNativeClient().getRemoteService();
        remoteService.register(OrderEventConsumer.class, this, CONCURRENT_INVOCATION_NUMBER);
    }

    /**
     * This method consumes saved order from remote service client and post a confirmed order message to a slack channel.
     *
     * @param order
     */
    @Override
    public void process(final Order order) {
        LOGGER.info("Received an order {}", order);
        String userName = order.getUser().getFirstName() + " " + order.getUser().getLastName().substring(0, 1).toUpperCase() + ".";
        // assume only one order item
        OrderItem orderItem = order.getOrderItems().iterator().next();
        OrderConfirmedMessage.OrderConfirmedMessageAttachment attachment =
                new OrderConfirmedMessage.OrderConfirmedMessageAttachment(
                        userName,
                        hniHomepage,
                        orderItem.getMenuItem().getName(),
                        orderItem.getQuantity().toString(),
                        order.getOrderDate().getTime());
        OrderConfirmedMessage message = new OrderConfirmedMessage(attachment);
        slackWebHookClient.postMessage(message);
    }
}
