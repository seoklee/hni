package org.hni.payment.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.hni.common.service.AbstractService;
import org.hni.order.dao.OrderDAO;
import org.hni.order.om.Order;
import org.hni.order.service.LockingService;
import org.hni.payment.dao.OrderPaymentDAO;
import org.hni.payment.dao.PaymentInstrumentDAO;
import org.hni.payment.om.OrderPayment;
import org.hni.payment.om.PaymentInfo;
import org.hni.payment.om.PaymentInstrument;
import org.hni.provider.om.Provider;
import org.hni.user.om.User;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class DefaultOrderPaymentService extends AbstractService<OrderPayment> implements OrderPaymentService {
	private static final Logger logger = LoggerFactory.getLogger(DefaultOrderPaymentService.class);
	private static final Long DEFAULT_CARD_LOCKOUT_MINS = 5L;
	
	private OrderPaymentDAO orderPaymentDao;
	private PaymentInstrumentDAO paymentInstrumentDao;
	private LockingService<RedissonClient> lockingService;
	private OrderDAO orderDao;
	
	@Inject
	public DefaultOrderPaymentService(OrderPaymentDAO orderPaymentDao, PaymentInstrumentDAO paymentInstrumentDao, OrderDAO orderDao, LockingService<RedissonClient> lockingService) {
		super(orderPaymentDao);
		this.orderPaymentDao = orderPaymentDao;
		this.paymentInstrumentDao = paymentInstrumentDao;
		this.orderDao = orderDao;
		this.lockingService = lockingService;
	}

	@Override
	public Order assignPayment(Collection<PaymentInfo> paymentInfos, User user) {
		Order order = null;
		for(PaymentInfo pi : paymentInfos) {
			logger.info(String.format("Recording payment %d - %d - %.2f", pi.getOrderId(), pi.getPaymentInstrumentId(), pi.getAmountUsed()));
			if (null == order) {
				order = orderDao.get(pi.getOrderId());
			}
			PaymentInstrument paymentInstrument = paymentInstrumentDao.get(pi.getPaymentInstrumentId());			
			OrderPayment orderPayment = new OrderPayment(order, paymentInstrument, pi.getAmountUsed(), user);
			
			BigDecimal cardTotal = BigDecimal.valueOf(paymentInstrument.getBalance()).subtract(BigDecimal.valueOf(pi.getAmountUsed()));
			paymentInstrument.setBalance(cardTotal.doubleValue());
			paymentInstrumentDao.save(paymentInstrument);

			this.save(orderPayment);
		}
		return order;
	}
		
	/**
	 * Return CARD, amount to use on this card
	 * @throws PaymentsExceededException 
	 */
	@Override
	public Collection<OrderPayment> paymentFor(Order order, Provider provider, Double amount, User user) throws PaymentsExceededException {
		Collection<PaymentInstrument> providerCards = paymentInstrumentDao.with(provider);
		Collection<OrderPayment> orderPayments = new HashSet<>();

		Double total = addOrderAmount(order);
		BigDecimal amountNeeded = BigDecimal.valueOf(amount);
		Double alreadyRequestedAmount = fromCache(order);
		
		if (addOrderAmount(order) <= 0) {
			logger.info(String.format("Order[%d] total amount is $%.2f.  Returning no payments", order.getId(), total));
			return orderPayments;
		}
		
		logger.info(String.format("Order[%d] total amount is $%.2f.  Requested amount is $%.2f", order.getId(), total, amount));
		
		// force the amount to be the max needed to finish the order payment.
		BigDecimal adjustedNeeded = BigDecimal.valueOf(total).subtract(BigDecimal.valueOf(alreadyRequestedAmount));
		if (adjustedNeeded.doubleValue() > amountNeeded.doubleValue()) {
			logger.info(String.format("Order[%d] total=$%.2f, alreadyRequestdPayments=$%.2f.  The amount needed $%.2f is greather than requested $%.2f, setting to greater value."
					, order.getId(), total, alreadyRequestedAmount, adjustedNeeded, amount));
			amountNeeded = adjustedNeeded;
		}
		if (providerCards.size() == 0) {
			logger.warn(String.format("There are no cards available for provider[%d] - %s", provider.getId(), provider.getName()));
		}
		for(PaymentInstrument paymentInstrument : providerCards) {
			if ( lockingService.acquireLock(lockingKey(paymentInstrument), DEFAULT_CARD_LOCKOUT_MINS)  ) {
				logger.info("locking card "+lockingKey(paymentInstrument));
				BigDecimal amountToDispense = calcAmountToDispense(paymentInstrument, amountNeeded);
				amountNeeded = amountNeeded.subtract(amountToDispense);
				
				OrderPayment orderPayment = new OrderPayment(order, paymentInstrument, amountToDispense.doubleValue(), user);
				logger.info("Dispensing payment "+orderPayment);
				orderPayments.add(orderPayment);

				paymentInstrument.setLastUsedDatetime(new Date());
				paymentInstrumentDao.save(paymentInstrument);
				logger.info("Still need "+amountNeeded.doubleValue());
				if ( amountNeeded.doubleValue() <= 0 ) {
					break;
				}
			}
		}
		
		// just doing the check for final launch, no lockouts
		totalAmountRequestedExceedsTotal(order, orderPayments);
		/*
		if (totalAmountRequestedExceedsTotal(order, orderPayments)) {
			logger.warn(String.format("You have requested more funds for order[%d] than you really need.  Shutting down this account...", order.getId()));
			clearCache(order); // clear payment info so somebody else doesn't get locked out for the same issue
			throw new PaymentsExceededException("You have requested more funds than you really need.  Shutting down this account...");
		}
		*/		
		return orderPayments;
	}
	
	@Override
	public Collection<OrderPayment> paymentsFor(Order order) { 
		return orderPaymentDao.paymentsFor(order);
	}

	private BigDecimal calcAmountToDispense(PaymentInstrument paymentInstrument, BigDecimal amount) {
		if (paymentInstrument.getBalance().doubleValue() >= amount.doubleValue()) {
			return amount;
		}
		// the amount on the card is less than we need, so return the full amount on the card.
		return BigDecimal.valueOf(paymentInstrument.getBalance());		
	}
	
	private String lockingKey(PaymentInstrument paymentInstrument) {
		return String.format("paymentInstrument:%d", paymentInstrument.getId());
	}
	
	private Double addOrderAmount(Order order) {
		double total = order.getOrderItems().stream().mapToDouble(o -> o.getMenuItem().getPrice()).sum();
		return total;
	}
	
	private boolean totalAmountRequestedExceedsTotal(Order order, Collection<OrderPayment> orderPayments) {
		Double total = addOrderAmount(order); 
		Double totalPrevPayments = fromCache(order);
		
		double paymentTotal = totalPrevPayments + orderPayments.stream().mapToDouble(o -> o.getAmount()).sum();
		// push all payments into cache
		toCache(order, paymentTotal);
		
		 
		logger.info(String.format("Order amount = $%.2f and total payments dispensed = $%.2f", total, paymentTotal ));
		return (paymentTotal > (1.25*total));
	}

	private String orderPaymentsKey(Order order) {
		return String.format("order-payments:%d", order.getId());
	}
	
	private void toCache(Order order, Double paymentTotal) {
		
		try {
			RBucket<Double> bucket = lockingService.getNativeClient().getBucket(orderPaymentsKey(order));
			bucket.set(paymentTotal, 2*DEFAULT_CARD_LOCKOUT_MINS, TimeUnit.MINUTES);
			logger.info(String.format("Caching OrderPayment:%s with %.2f", orderPaymentsKey(order),paymentTotal));
		} catch(Exception e) {
			// log it and move on
			logger.warn("Unable to push orderPayments into cache for "+orderPaymentsKey(order));
		}
	}
	
	private Double fromCache(Order order) {	
		try {
			RBucket<Double> bucket = lockingService.getNativeClient().getBucket(orderPaymentsKey(order));
			Double value = bucket.get();
			if (null == value) {
				return value = 0.0;
			}
			logger.info(String.format("Returning previously dispensed payments for %s of $%.2f", orderPaymentsKey(order), order.getId(), value));
			return value;
		} catch(Exception e) {
			logger.warn("Unable to get orderPayments from cache for "+orderPaymentsKey(order));
			return 0.0;
		}
	}
	
	private void clearCache(Order order) {
		lockingService.getNativeClient().getBucket(orderPaymentsKey(order)).delete();
	}
	
}
