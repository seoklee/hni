package org.hni.provider.dao;

import org.hni.common.dao.BaseDAO;
import org.hni.provider.om.LocationQueryParams;
import org.hni.provider.om.Provider;
import org.hni.provider.om.ProviderLocation;

import java.util.Collection;

public interface ProviderLocationDAO extends BaseDAO<ProviderLocation> {

	Collection<ProviderLocation> with(Provider provider);

    /**
     * get provider location by customers address
     * @param locationQuery
     * @return
     */
    Collection<ProviderLocation> providersNearCustomer(LocationQueryParams locationQuery);

}
