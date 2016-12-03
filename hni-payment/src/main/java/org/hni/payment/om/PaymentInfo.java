package org.hni.payment.om;

import org.hni.order.om.Order;
import org.hni.user.om.User;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * This is to capture the payment into coming from the UI
 * @author jeff3parker
 *
 */
public class PaymentInfo {

	private Long orderId;
	private Long paymentInstrumentId;
	private Double amountUsed;
	
	public PaymentInfo() {}

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public Long getPaymentInstrumentId() {
		return paymentInstrumentId;
	}

	public void setPaymentInstrumentId(Long paymentInstrumentId) {
		this.paymentInstrumentId = paymentInstrumentId;
	}

	public Double getAmountUsed() {
		return amountUsed;
	}

	public void setAmountUsed(Double amountUsed) {
		this.amountUsed = amountUsed;
	}

	@Override
	public String toString() {
		return String.format("PaymentInfo {orderId:%d, paymentInstrumentId:%d, amountUsed:$.%.2f}", orderId, paymentInstrumentId, amountUsed);
	}
}
