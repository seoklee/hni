package org.hni.provider.service;

import org.hni.provider.om.Provider;
import org.hni.provider.om.ProviderLocation;
import org.hni.user.om.Address;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

import java.util.Collection;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * The schema for testing is located in the hni-schema project which gets shared across
 * all test cases.
 * 
 * @author j2parke
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:test-applicationContext.xml"} )
@Transactional
public class TestProviderLocationService {

	@Inject private ProviderService providerService;
	@Inject private ProviderLocationService providerLocationService;

	@Test
	public void testGetProviderLocation() {
		ProviderLocation providerLocation = providerLocationService.get(1L);
		assertNotNull(providerLocation);
	}

	/*
	@Test
	public void testAddAddress() {
		ProviderLocation providerLocation = providerLocationService.get(1L);
		assertNotNull(providerLocation);
		assertNotNull(providerLocation.getAddress());

		providerLocation.getAddress().add( new Address("address1", "address2", "city", "AR", "zip") );
		providerLocationService.save(providerLocation);

		ProviderLocation plCheck = providerLocationService.get(1L);
		assertNotNull(plCheck);
		assertTrue(plCheck.getAddresses().size() > 0);

	}
	*/

	@Test
	public void testAddLocationToProvider() {
		Provider provider = providerService.get(1L);
		assertNotNull(provider);

		ProviderLocation pl = new ProviderLocation();
		pl.setCreated(new Date());
		pl.setCreatedById(1L);
		pl.setName("test new location");
		pl.setProvider(provider);

		pl = providerLocationService.save(pl);

		Collection<ProviderLocation> list = providerLocationService.with(provider);
		assertNotNull(list);
		assertTrue(list.size() > 0);

	}

	@Test
	public void testGetCustomerAddress() throws Exception {
		Address address = providerLocationService.searchCustomerAddress("bridle view way ohcolumbus");
		assertNotNull(address);
	}

//	@Test
	public void testGetProviderLocationByCustomerId() {
		Address address = new Address();
		address.setAddress1("bridle view way");
		address.setState("oh");
		address.setCity("columbus");
		address.setLatitude(-82.9834599);
		address.setLongitude(40.1376158);
		Collection<ProviderLocation> providerLocations = providerLocationService.providersNearCustomer(address, 3, 0, 0);
		assertTrue(providerLocations.size() > 0);

//		providerLocations = providerLocationService.providersNearCustomer("reston town center reston va", 1, 0, 0);
//		assertTrue(providerLocations.size() > 0);
	}
}
