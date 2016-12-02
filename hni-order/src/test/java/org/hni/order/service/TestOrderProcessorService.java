package org.hni.order.service;

import javax.inject.Inject;

import org.hni.user.om.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-applicationContext.xml" })
public class TestOrderProcessorService {
	private static final Logger logger = LoggerFactory.getLogger(TestOrderProcessorService.class);

	@Inject
	private OrderService orderService;

	@Inject
	private DefaultOrderProcessor orderProcessor;

	@Test
	public void maxOrdersReached() {
		User user = new User();
		logger.debug("Running test");
		user.setId(9L);

		logger.debug("hasCodes=" + orderService.hasActiveActivationCodes(user));
		logger.debug("maxMeals=" + orderService.maxDailyOrdersReached(user));

		Assert.assertTrue(orderService.hasActiveActivationCodes(user));
		Assert.assertTrue(orderService.maxDailyOrdersReached(user));
	}
	@Test
	public void maxOrdersNotReached() {
		User user = new User();
		logger.debug("Running test");
		user.setId(10L);

		logger.debug("hasCodes=" + orderService.hasActiveActivationCodes(user));
		logger.debug("maxMeals=" + orderService.maxDailyOrdersReached(user));

		Assert.assertTrue(orderService.hasActiveActivationCodes(user));
		Assert.assertFalse(orderService.maxDailyOrdersReached(user));
	}

	@Test
	public void testStatusOnCompletedOrder() {
		User user = new User();
		logger.debug("Running test");
		user.setId(11L);

		String message = orderProcessor.processMessage(user, "STATUS");
		logger.debug(message);
	
	}
}
