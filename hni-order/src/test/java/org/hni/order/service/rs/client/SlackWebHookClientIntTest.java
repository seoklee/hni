package org.hni.order.service.rs.client;

import org.hni.order.om.rs.OrderConfirmedMessage;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-applicationContext.xml", "classpath:redis.ctx.xml"})
public class SlackWebHookClientIntTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SlackWebHookClientIntTest.class);

    @Inject
    private SlackWebHookClient client;

    @Test
    @Ignore
    public void testSendAlert() throws Exception {
        OrderConfirmedMessage.OrderConfirmedMessageAttachment attachment =
                new OrderConfirmedMessage.OrderConfirmedMessageAttachment(
                        "John Doe",
                        "https://link.to.order.place.page/",
                        "chicken soft tacos",
                        "2",
                        System.currentTimeMillis());
        OrderConfirmedMessage message = new OrderConfirmedMessage(attachment);
        client.postMessage(message);
    }
}
