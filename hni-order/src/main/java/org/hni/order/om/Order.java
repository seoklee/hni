package org.hni.order.om;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hni.common.om.Persistable;
import org.hni.order.om.type.OrderStatus;
import org.hni.provider.om.ProviderLocation;
import org.hni.user.om.User;

/**
 * Represents a request for something/a meal.  The order is related to a User/client,
 * a ProviderLocation and one or more MenuItem via the OrderItem.
 * 
 * @author j2parke
 *
 */
@Entity
@Table(name = "orders")
public class Order implements Persistable, Serializable {

	private static final long serialVersionUID = 6662915064153704996L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;

	//@Column(name="user_id") private Long userId;
	@Column(name="order_date") private Date orderDate;
	@Column(name="ready_date") private Date readyDate;
	@Column(name="pickup_date") private Date pickupDate;
	@Column(name="subtotal") private Double subTotal;
	@Column(name="tax") private Double tax;
	@Column(name="created_by") private Long createdById;
	@Column(name="status_id") private Long statusId;
	
	@ManyToOne
	@JoinColumn(name="user_id", referencedColumnName = "id")
	private User user;
	
	@ManyToOne
	@JoinColumn(name="provider_location_id", referencedColumnName = "id")
	private ProviderLocation providerLocation;

	@OneToMany(fetch=FetchType.EAGER, mappedBy="order", cascade = {CascadeType.ALL}, orphanRemoval=true)
	private Set<OrderItem> orderItems = new HashSet<>();
	
	public Order() {}
	public Order(Long id) {
		this.id = id;
	}
	@Override
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}

	public void setUserId(Long userId) {
		this.user = new User(userId);
	}

	public Date getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(Date orderDate) {
		this.orderDate = orderDate;
	}

	public Date getReadyDate() {
		return readyDate;
	}

	public void setReadyDate(Date readyDate) {
		this.readyDate = readyDate;
	}

	public Date getPickupDate() {
		return pickupDate;
	}

	public void setPickupDate(Date pickupDate) {
		this.pickupDate = pickupDate;
	}

	public Double getSubTotal() {
		return subTotal;
	}

	public void setSubTotal(Double subTotal) {
		this.subTotal = subTotal;
	}

	public Double getTax() {
		return tax;
	}

	public void setTax(Double tax) {
		this.tax = tax;
	}

	public Long getCreatedById() {
		return createdById;
	}

	public void setCreatedById(Long createdById) {
		this.createdById = createdById;
	}

	public ProviderLocation getProviderLocation() {
		return providerLocation;
	}

	public void setProviderLocation(ProviderLocation providerLocation) {
		this.providerLocation = providerLocation;
	}
	
	public Double getTotal() {
		return this.subTotal + this.tax;
	}

	public Set<OrderItem> getOrderItems() {
		return orderItems;
	}

	public void setOrderItems(Set<OrderItem> orderItems) {
		this.orderItems = orderItems;
	}
	public Long getStatusId() {
		return statusId;
	}
	public void setStatusId(Long statusId) {
		this.statusId = statusId;
	}
	public void setStatus(OrderStatus orderStatus) {
		this.statusId = orderStatus.getId();
	}
	public OrderStatus getOrderStatus() {
		return OrderStatus.get(this.statusId);
	}
	@Override
	public String toString() {
		return "Order [id=" + id + ", orderDate=" + orderDate + ", readyDate=" + readyDate + ", pickupDate="
				+ pickupDate + ", subTotal=" + subTotal + ", tax=" + tax + ", createdById=" + createdById
				+ ", statusId=" + statusId + ", user=" + user + ", providerLocation=" + providerLocation
				+ ", orderItems=" + orderItems + "]";
	}
	
}
