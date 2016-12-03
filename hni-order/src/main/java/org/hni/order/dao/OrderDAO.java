package org.hni.order.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;

import org.hni.common.dao.BaseDAO;
import org.hni.order.om.Order;
import org.hni.order.om.type.OrderStatus;
import org.hni.provider.om.Provider;
import org.hni.user.om.User;

public interface OrderDAO extends BaseDAO<Order> {

	Collection<Order> get(User user);

	Collection<Order> get(User user, LocalDate fromDate, LocalDate toDate);
	Collection<Order> get(User user, LocalDateTime fromDate, LocalDateTime toDate);

	Collection<Order> get(Provider provider, LocalDateTime fromDate, LocalDateTime toDate);

	Collection<Order> with(OrderStatus orderStatus);

	Collection<Order> with(Provider provider, OrderStatus orderStatus);

}
