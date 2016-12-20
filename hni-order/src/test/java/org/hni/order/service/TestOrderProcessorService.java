package org.hni.order.service;

import java.util.Collection;

import javax.inject.Inject;

import org.hni.order.om.Order;
import org.hni.user.om.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-applicationContext.xml", "classpath:redis.ctx.xml"})
public class TestOrderProcessorService {
	private static final Logger logger = LoggerFactory.getLogger(TestOrderProcessorService.class);

	@Inject
	private OrderService orderService;

	@Inject
	private OrderProcessor orderProcessor;

	@Test
	public void maxOrdersReached() {
		User user = new User();
		logger.debug("##### maxOrdersReached #####");
		user.setId(9L);

		logger.debug("hasCodes=" + orderService.hasActiveActivationCodes(user));
		logger.debug("maxMeals=" + orderService.maxDailyOrdersReached(user));

		Assert.assertTrue(orderService.hasActiveActivationCodes(user));
		Assert.assertTrue(orderService.maxDailyOrdersReached(user));
	}

	@Test
	public void orderedYesterday() {
		User user = new User();
		logger.debug("##### orderedYesterday #####");
		user.setId(12L);

		logger.debug("hasCodes=" + orderService.hasActiveActivationCodes(user));
		logger.debug("maxMeals=" + orderService.maxDailyOrdersReached(user));

		Assert.assertTrue(orderService.hasActiveActivationCodes(user));
		Assert.assertFalse(orderService.maxDailyOrdersReached(user));
	}

	@Test
	public void orderedRecently() {
		User user = new User();
		logger.debug("##### orderedRecently ##### ");
		user.setId(13L);
		Collection<Order> list = orderService.get(user);
		
		for(Order order : list) {
			logger.debug(order.toString());
		}
		
		logger.debug("hasCodes=" + orderService.hasActiveActivationCodes(user));
		logger.debug("maxMeals=" + orderService.maxDailyOrdersReached(user));

		Assert.assertTrue(orderService.hasActiveActivationCodes(user));
		Assert.assertTrue(orderService.maxDailyOrdersReached(user));
	}

	@Test
	public void maxOrdersNotReached() {
		User user = new User();
		logger.debug(" ##### maxOrdersNotReached ##### ");
		user.setId(10L);
		boolean maxMeals = orderService.maxDailyOrdersReached(user);

		logger.debug("hasCodes=" + orderService.hasActiveActivationCodes(user));
		logger.debug("maxMeals=" + orderService.maxDailyOrdersReached(user));

		Assert.assertTrue(orderService.hasActiveActivationCodes(user));
		Assert.assertFalse(orderService.maxDailyOrdersReached(user));
		
	}

	@Test
	public void testStatusOnCompletedOrder() {
		User user = new User();
		logger.debug(" ##### testStatusOnCompletedOrder ##### ");
		user.setId(11L);

		String message = orderProcessor.processMessage(user, "STATUS");
		logger.debug(message);
	
	}
}
