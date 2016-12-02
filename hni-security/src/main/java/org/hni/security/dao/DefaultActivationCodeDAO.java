package org.hni.security.dao;

import org.hni.common.dao.AbstractDAO;
import org.hni.security.om.ActivationCode;
import org.hni.user.om.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

@Component
public class DefaultActivationCodeDAO extends AbstractDAO<ActivationCode> implements ActivationCodeDAO {
	private static final Logger logger = LoggerFactory.getLogger(ActivationCodeDAO.class);
	
	public DefaultActivationCodeDAO() {
		super(ActivationCode.class);		
	}

	public ActivationCode getByActivationCode(String actCode) {
		try {
			Query q = em.createQuery("SELECT x FROM ActivationCode x WHERE x.activationCode = :actCode")
				.setParameter("actCode", actCode);
            return (ActivationCode) q.getSingleResult();
		} catch(NoResultException e) {
			return null;
		}
	}

	@Override
	public List<ActivationCode> getByUser(User user) {
		try {
			Query q = em.createQuery("SELECT x FROM ActivationCode x WHERE x.user.id = :userId AND x.mealsRemaining > 0 AND x.enabled = true")
				.setParameter("userId", user.getId());
			return q.getResultList();
		} catch (NoResultException e) {
			return Collections.emptyList();
		}
	}

}
