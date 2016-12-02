package org.hni.order.service;

import org.hni.events.service.om.Event;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-applicationContext.xml"})
public class DefaultOrderProcessServiceIntTest {
    private static final Logger logger = LoggerFactory.getLogger(DefaultOrderProcessServiceIntTest.class);

    @Inject
    private OrderProcessor orderProcessor;

    @Test
    @Ignore // disable the test since the test-data.sql does not have correct setting for activation code. always get 'the maximum number of orders'
    public void invalid_address_fail() {
        Event event = Event.createEvent("text/plain", "123-456-7830", "MEAL");
        String returnstr = orderProcessor.handleEvent(event);
        Assert.assertEquals("Yes! Let's get started to order a meal for you. Reply with your location (e.g. #3 Smith St. 72758) or ENDMEAL to quit", returnstr);

        event = Event.createEvent("text/plain", "123-456-7830", "7273477383883884");
        returnstr = orderProcessor.handleEvent(event);
        Assert.assertEquals("There are no providers near your location. Reply with new location or ENDMEAL to quit.", returnstr);
    }
}
