package org.hni.events.service.dao;

import org.hni.common.dao.BaseDAO;
import org.hni.events.service.om.RegistrationState;

public interface RegistrationStateDAO extends BaseDAO<RegistrationState> {

    RegistrationState getByPhoneNumber(String phoneNumber);

    RegistrationState deleteByPhoneNumber(String phoneNumber);

}
