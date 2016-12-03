package org.hni.order.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.hni.common.DateUtils;
import org.hni.common.dao.AbstractDAO;
import org.hni.order.om.Order;
import org.hni.order.om.type.OrderStatus;
import org.hni.provider.om.Provider;
import org.hni.user.om.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DefaultOrderDAO extends AbstractDAO<Order> implements OrderDAO {
	private static final Logger logger = LoggerFactory.getLogger(OrderDAO.class);
	
	public DefaultOrderDAO() {
		super(Order.class);
	}

	@Override
	public Collection<Order> get(User user) {
		//logger.info("Searching for orders between {} and {}", fromDate.toString(), toDate.toString());
		try {
			Query q = em.createQuery("SELECT x FROM Order x WHERE x.user.id = :userId")
					.setParameter("userId", user.getId());
			return q.getResultList();
		} catch(NoResultException e) {
			return Collections.emptyList();
		}
	}

	@Override
	public Collection<Order> get(User user, LocalDate fromDate, LocalDate toDate) {
		try {
			Query q = em.createQuery("SELECT x FROM Order x WHERE x.user.id = :userId AND x.orderDate BETWEEN :fromDate AND :toDate")
				.setParameter("userId", user.getId())
				.setParameter("fromDate", DateUtils.asDate(fromDate))
				.setParameter("toDate", DateUtils.asDate(toDate));
			return q.getResultList();
		} catch(NoResultException e) {
			return Collections.emptyList();
		}
	}

	@Override
	public Collection<Order> get(User user, LocalDateTime fromDate, LocalDateTime toDate) {
		try {
			Query q = em.createQuery("SELECT x FROM Order x WHERE x.user.id = :userId AND x.orderDate BETWEEN :fromDate AND :toDate")
				.setParameter("userId", user.getId())
				.setParameter("fromDate", DateUtils.asDate(fromDate))
				.setParameter("toDate", DateUtils.asDate(toDate));
			return q.getResultList();
		} catch(NoResultException e) {
			return Collections.emptyList();
		}
	}

	@Override
	public Collection<Order> get(Provider provider, LocalDateTime fromDate, LocalDateTime toDate) {
		try {
			Query q = em.createQuery("SELECT x FROM Order x WHERE x.orderDate BETWEEN :fromDate AND :toDate AND x.providerLocation.provider.id = :providerId ")
					.setParameter("providerId", provider.getId())
					.setParameter("fromDate", DateUtils.asDate(fromDate))
					.setParameter("toDate", DateUtils.asDate(toDate));
			return q.getResultList();
		} catch (NoResultException e) {
			return Collections.emptyList();
		}
	}
	
	@Override
	public Collection<Order> with(OrderStatus orderStatus) {
		try {
			Query q = em.createQuery("SELECT x FROM Order x WHERE x.statusId = :statusId")
					.setParameter("statusId", orderStatus.getId());
			return q.getResultList();
		} catch (NoResultException e) {
			return Collections.emptyList();
		}
	}

	@Override
	public Collection<Order> with(Provider provider, OrderStatus orderStatus) {
		try {
			Query q = em.createQuery("SELECT x FROM Order x WHERE x.providerLocation.provider.id = :providerId AND x.statusId = :statusId")
					.setParameter("providerId", provider.getId())
					.setParameter("statusId", orderStatus.getId());
			return q.getResultList();
		} catch (NoResultException e) {
			return Collections.emptyList();
		}
	}
	
}
