package org.hni.order.dao;

import org.hni.common.dao.AbstractDAO;
import org.hni.order.om.PartialOrder;
import org.hni.user.om.User;
import org.springframework.stereotype.Component;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import java.util.List;

/**
 * The DAO for PartialOrders
 */
@Component
public class DefaultPartialOrderDAO extends AbstractDAO<PartialOrder> {
    public DefaultPartialOrderDAO() {
        super(PartialOrder.class);
    }

    public PartialOrder byUser(User user) {
        if ( null == user ) {
            return null;
        }
        try {
            Query q = em.createQuery("SELECT x FROM PartialOrder x WHERE x.user.id  = :user ")
                    .setParameter("user", user.getId());
            List resultList = q.getResultList();
            if (resultList != null && !resultList.isEmpty()) {
                return (PartialOrder) resultList.get(0);
            }
        } catch (NoResultException e) {
        }
        return null;
    }
}
