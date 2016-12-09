package org.hni.payment.service;

import java.util.Collection;
import java.util.Optional;

import org.hni.common.service.BaseService;
import org.hni.order.om.Order;
import org.hni.payment.om.OrderPayment;
import org.hni.payment.om.PaymentInfo;
import org.hni.provider.om.Provider;
import org.hni.user.om.User;

public interface OrderPaymentService extends BaseService<OrderPayment > {

	Collection<OrderPayment> paymentFor(Order order, Provider provider, Double amount, User user) throws PaymentsExceededException;
	Optional<OrderPayment> paymentFor(Order order, User user) throws PaymentsExceededException;
	
	Order assignPayment(Collection<PaymentInfo> paymentInfos, User user);
	Collection<OrderPayment> paymentsFor(Order order);
}
