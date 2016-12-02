package org.hni.provider.dao;

import org.hni.common.dao.AbstractDAO;
import org.hni.provider.om.LocationQueryParams;
import org.hni.provider.om.Provider;
import org.hni.provider.om.ProviderLocation;
import org.springframework.stereotype.Component;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import java.util.Collection;
import java.util.Collections;

@Component
public class DefaultProviderLocationDAO extends AbstractDAO<ProviderLocation> implements ProviderLocationDAO {

	protected DefaultProviderLocationDAO() {
		super(ProviderLocation.class);
	}

	@Override
	public Collection<ProviderLocation> with(Provider provider) {
		try {
			Query q = em.createQuery("SELECT x FROM ProviderLocation x WHERE x.provider.id = :providerId")
				.setParameter("providerId", provider.getId());
			return q.getResultList();
		} catch(NoResultException e) {
			return Collections.emptyList();
		}
	}

	

    @Override
    public Collection<ProviderLocation> providersNearCustomer(LocationQueryParams locationQuery) {

        try {

            String queryString = new StringBuilder(
                            "SELECT pl.* FROM provider_locations pl " +
                            " WHERE pl.address_id in " +
                            " ( select new_addr.id from " +
                            " (SELECT id, " +
                            " ( acos(sin(:custLatRad) * sin(radians(latitude)) + " +
                            " cos(:custLatRad) * cos(radians(latitude)) * cos(radians(longitude) - :custLongRad))   ) as distance" +
                    
                            " FROM addresses " +
                            " WHERE (latitude >= :minLatDeg AND latitude <= :maxLatDeg) AND (longitude >= :minLongDeg " +
                            (locationQuery.isMeridian180WithinDistance() ? "OR" : "AND") +
                            " longitude <= :maxLongDeg) "  +
                            " group by id having distance <= :dist order by distance" +
                            "   )" +
                            "  as new_addr )")
                    .toString();
            

            Query q = em.createNativeQuery(queryString, ProviderLocation.class)
                    .setParameter("minLatDeg", locationQuery.getMinLattitudeDeg())  //minLat;
                    .setParameter("maxLatDeg", locationQuery.getMaxLattitudeDeg())  //maxLat 
                    .setParameter("minLongDeg", locationQuery.getMinLongitudeDeg()) //minLong
                    .setParameter("maxLongDeg", locationQuery.getMaxLongitudeDeg()) //maxLong
                    .setParameter("custLatRad", locationQuery.getCustomerLattitudeRad())   
                    .setParameter("custLongRad", locationQuery.getCustomerLongitudeRad())
                    .setParameter("dist", locationQuery.getDistnaceByRadius());

            return q.getResultList();
        } catch(NoResultException e) {
            return Collections.emptyList();
        }
    }

}
