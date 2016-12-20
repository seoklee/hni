package org.hni.order.service;

import org.hni.events.service.om.Event;
import org.hni.order.om.Order;
import org.hni.order.om.OrderItem;
import org.hni.order.om.type.OrderStatus;
import org.hni.provider.om.MenuItem;
import org.hni.user.om.User;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RRemoteService;
import org.redisson.api.RedissonClient;
import org.redisson.api.RemoteInvocationOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-applicationContext.xml", "classpath:redis.ctx.xml"})
public class DefaultOrderProcessIntTest {
    private static final Logger logger = LoggerFactory.getLogger(DefaultOrderProcessIntTest.class);

    @Inject
    private OrderProcessor orderProcessor;

    @Inject
    private LockingService<RedissonClient> redissonClient;

    @Test
    @Ignore
    // disable the test since the test-data.sql does not have correct setting for activation code. always get 'the maximum number of orders'
    public void invalidAddress_fail() {
        Event event = Event.createEvent("text/plain", "123-456-7830", "MEAL");
        String returnstr = orderProcessor.handleEvent(event);
        Assert.assertEquals("Yes! Let's get started to order a meal for you. Reply with your location (e.g. #3 Smith St. 72758) or ENDMEAL to quit", returnstr);

        event = Event.createEvent("text/plain", "123-456-7830", "7273477383883884");
        returnstr = orderProcessor.handleEvent(event);
        Assert.assertEquals("There are no providers near your location. Reply with new location or ENDMEAL to quit.", returnstr);
    }

    @Test
    @Ignore
    public void testOrderEventConsumer() throws InterruptedException {
        RRemoteService remoteService = redissonClient.getNativeClient().getRemoteService();
        OrderEventConsumer orderEventConsumer = remoteService.get(OrderEventConsumer.class, RemoteInvocationOptions.defaults().noResult());
        Order order = new Order();
        order.setOrderDate(new Date());
        order.setStatus(OrderStatus.OPEN);
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        order.setUser(user);
        Set<OrderItem> items = new HashSet<>();
        OrderItem item = new OrderItem();
        item.setQuantity(2L);
        item.setMenuItem(new MenuItem("chicken soft tacos, test meal", null, null, null));
        items.add(item);
        order.setOrderItems(items);
        orderEventConsumer.process(order);
        Thread.sleep(10000L);
    }
}
