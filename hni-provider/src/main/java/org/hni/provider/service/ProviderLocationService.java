package org.hni.provider.service;

import org.hni.common.service.BaseService;
import org.hni.provider.om.Provider;
import org.hni.provider.om.ProviderLocation;

import java.util.Collection;

public interface ProviderLocationService extends BaseService<ProviderLocation> {

	Collection<ProviderLocation> with(Provider provider);
	Collection<ProviderLocation> providersNearCustomer(String customerAddress, int itemsPerPage, double distance, double radius);
}
