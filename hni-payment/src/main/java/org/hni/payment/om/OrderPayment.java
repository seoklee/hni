package org.hni.payment.om;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hni.common.om.Persistable;
import org.hni.order.om.Order;
import org.hni.user.om.User;

@Entity
@Table(name = "order_payments")
public class OrderPayment implements Serializable, Persistable {

	private static final long serialVersionUID = -1036619624568269599L;

	@EmbeddedId private OrderPaymentPK id;

	@Column(name="amount") private Double amount;
	@Column(name="created_by") private Long createdById;
	@Column(name="created_date") private Date createdDatetime;
	
	public OrderPayment() {}
	public OrderPayment(OrderPaymentPK id, Double amount, Long createdById, Date createdDatetime) {
		this.id = id;
		this.amount = amount;
		this.createdById = createdById;
		this.createdDatetime = createdDatetime;
	}

	@Override
	public String toString() {
		return String.format("OrderPayment: orderId:%d, cardId:%d, amount:%.2f ", id.getOrder().getId(), id.getPaymentInstrument().getId(), amount);
	}
	public OrderPayment(Order order, PaymentInstrument paymentInstrument, Double amount, User user) {
		this(new OrderPaymentPK(order, paymentInstrument), amount, user.getId(), new Date());
	}
	public OrderPaymentPK getId() {
		return id;
	}

	public void setId(OrderPaymentPK id) {
		this.id = id;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public Long getCreatedById() {
		return createdById;
	}

	public void setCreatedById(Long createdById) {
		this.createdById = createdById;
	}

	public Date getCreatedDatetime() {
		return createdDatetime;
	}

	public void setCreatedDatetime(Date createdDatetime) {
		this.createdDatetime = createdDatetime;
	}
	
	
}
