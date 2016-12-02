package org.hni.provider.service;

import org.apache.commons.lang3.StringUtils;
import org.hni.common.service.AbstractService;
import org.hni.provider.dao.ProviderLocationDAO;
import org.hni.provider.om.AddressException;
import org.hni.provider.om.LocationQueryParams;
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
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
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
    public Collection<ProviderLocation> providersNearCustomer(String customerAddress, int itemsPerPage, double distance
            , double radius) {

        if(!StringUtils.isBlank(customerAddress)) {
            
            Address addrpoint = geoCodingService.resolveAddress(customerAddress).orElse(null);

            if (addrpoint != null) {
                LocationQueryParams locationQuery = transFormLocationToQueryObject(addrpoint, radius, (distance * 1.60934));
                return providerLocationDao.providersNearCustomer(locationQuery);
            } else {
                throw new AddressException("Unable to resolve address");
            }
            
        } else {
            throw new AddressException("Invalid address provided");
        }
	    
    }
	
	/**
	 * method takes latitude longitude of customer
	 * find boundaries and create an object with all search parameters
	 * @param addrpoint
	 * @param radius
	 * @param distance
	 * @return
	 */
	protected LocationQueryParams transFormLocationToQueryObject(Address addrpoint, double radius, double distance) {
	    
	    
	    GeoLocation customerLocation = GeoLocation.fromDegrees(new Double(addrpoint.getLatitude()), new Double(addrpoint.getLongitude()));

        GeoLocation[] boundingCoordinates = customerLocation.boundingCoordinates(distance, radius);
	    
	    LocationQueryParams locationQueryParams = new LocationQueryParams();
	    
	    locationQueryParams.setCustomerLattitudeRad(customerLocation.getLatitudeInRadians());
	    locationQueryParams.setCustomerLongitudeRad(customerLocation.getLongitudeInRadians());
	    
	    locationQueryParams.setMinLattitudeDeg(boundingCoordinates[0].getLatitudeInDegrees());
	    locationQueryParams.setMinLattitudeRad(boundingCoordinates[0].getLatitudeInRadians());
	    
	    locationQueryParams.setMinLongitudeDeg(boundingCoordinates[0].getLongitudeInDegrees());
        locationQueryParams.setMinLongitudeRad(boundingCoordinates[0].getLongitudeInRadians());
	    
	    
        locationQueryParams.setMaxLattitudeDeg(boundingCoordinates[1].getLatitudeInDegrees());
        locationQueryParams.setMaxLattitudeRad(boundingCoordinates[1].getLatitudeInRadians());
        
        locationQueryParams.setMaxLongitudeDeg(boundingCoordinates[1].getLongitudeInDegrees());
        locationQueryParams.setMaxLongitudeRad(boundingCoordinates[1].getLongitudeInRadians());
        
        locationQueryParams.setMeridian180WithinDistance(boundingCoordinates[0].getLongitudeInRadians() > 
            boundingCoordinates[1].getLongitudeInRadians());
        
        locationQueryParams.setDistnaceByRadius(distance / radius);
	    
        return locationQueryParams;
	}


}
