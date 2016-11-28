package org.hni.events.service.dao;

import org.hni.common.dao.AbstractDAO;
import org.hni.events.service.om.RegistrationState;
import org.springframework.stereotype.Component;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;

@Component
public class DefaultRegistrationStateDAO extends AbstractDAO<RegistrationState> implements RegistrationStateDAO {

    public DefaultRegistrationStateDAO() {
        super(RegistrationState.class);
    }

    public RegistrationState getByPhoneNumber(String phoneNumber) {
        try {
            Query q = em.createQuery("SELECT x FROM RegistrationState x WHERE x.phoneNumber  = :phoneNumber ")
                    .setParameter("phoneNumber", phoneNumber);
            List resultList = q.getResultList();
            if (resultList != null && !resultList.isEmpty()) {
                return (RegistrationState) resultList.get(0);
            }
        } catch (NoResultException e) {
        }
        return null;
    }

    public RegistrationState deleteByPhoneNumber(String phoneNumber) {
        RegistrationState rs = getByPhoneNumber(phoneNumber);
        return delete(rs);
    }
}
