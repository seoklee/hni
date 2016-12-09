package org.hni.provider.service;

import org.hni.common.service.AbstractService;
import org.hni.provider.dao.ProviderLocationDAO;
import org.hni.provider.om.Provider;
import org.hni.provider.om.ProviderLocation;
import org.hni.user.om.Address;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Collection;

@Service
@Transactional(propagation = Propagation.REQUIRED)
public class DefaultProviderLocationService extends AbstractService<ProviderLocation> implements ProviderLocationService {

    private ProviderLocationDAO providerLocationDao;

    @Autowired
    private GeoCodingService geoCodingService;

    @Inject
    public DefaultProviderLocationService(ProviderLocationDAO providerLocationDao) {
        super(providerLocationDao);
        this.providerLocationDao = providerLocationDao;
    }

    @Override
    public Collection<ProviderLocation> with(Provider provider) {
        return this.providerLocationDao.with(provider);
    }

    @Override
    public Address searchCustomerAddress(String customerAddress) {
        return geoCodingService.resolveAddress(customerAddress).orElse(null);
    }

    @Override
    public Collection<ProviderLocation> providersNearCustomer(Address customerAddress, int itemsPerPage, double distance, double radius) {
        return providerLocationDao.providersNearCustomer(customerAddress, itemsPerPage);
    }

}
