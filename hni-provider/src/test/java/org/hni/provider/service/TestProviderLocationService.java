package org.hni.provider.service;

import org.hni.provider.om.AddressException;
import org.hni.provider.om.Provider;
import org.hni.provider.om.ProviderLocation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

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
	
	@Test(expected = AddressException.class)
    public void testGetProviderLocationByCustomerAddr_nullAddress() {
        providerLocationService.providersNearCustomer(null, 1, 10, 6371.01);
        fail("Exception is expected");
    }
	
	@Test(expected = AddressException.class)
    public void testGetProviderLocationByCustomerAddr_emptyAddress() {
        providerLocationService.providersNearCustomer("  ", 1, 10, 6371.01);
        fail("Exception is expected");
    }
	
	@Test(expected = AddressException.class)
    public void testGetProviderLocationByCustomerAddr_invalidAddress() {
        providerLocationService.providersNearCustomer("not a good addres", 1, 10, 6371.01);
        fail("Exception is expected");
    }

	@Test
	public void testGetProviderLocationByCustomerAddr() {
		Collection<ProviderLocation> providerLocations = providerLocationService.providersNearCustomer("bridle view way ohcolumbus", 1, 10, 6371.01);
		assertTrue(providerLocations.size() > 0);

		providerLocations = providerLocationService.providersNearCustomer("reston town center reston va", 1, 10, 6371.01);
		assertTrue(providerLocations.size() > 0);
	}
	
	
	@Test
    public void testGetProviderLocationByCustomerAddr_found_in2miles() {
        Collection<ProviderLocation> providerLocations = providerLocationService.providersNearCustomer("8914 Centreville Rd Manassas, VA", 1, 2, 6371.01);
        assertTrue(providerLocations.size() > 0);
        
        ProviderLocation providerLoc = providerLocations.iterator().next();
        assertEquals("MANASSAS", providerLoc.getAddress().getCity().toUpperCase());

    }
	
	@Test
    public void testGetProviderLocationByCustomerAddr_not_found_in2miles() {
        Collection<ProviderLocation> providerLocations = providerLocationService.providersNearCustomer("10864 Sudley Manor Dr, Manassas, VA", 1, 2, 6371.01);
        assertTrue(providerLocations.size() == 0);

    }
	
	@Test
    public void testGetProviderLocationByCustomerAddr_found_in10miles() {
        Collection<ProviderLocation> providerLocations = providerLocationService.providersNearCustomer("10864 Sudley Manor Dr, Manassas, VA", 1, 10, 6371.01);
        assertTrue(providerLocations.size() > 0);
        
        ProviderLocation providerLoc = providerLocations.iterator().next();
        assertEquals("MANASSAS", providerLoc.getAddress().getCity().toUpperCase());
    }
	
	
	@Test
    public void testGetProviderLocationByCustomerAddr_found_multiple() {
	    // test out of multiple providers first one is nearest
        Collection<ProviderLocation> providerLocations = providerLocationService.providersNearCustomer("bridle view way oh columbus", 1, 10, 6371.01);
        assertTrue(providerLocations.size() > 0);

        //nearest
        Iterator<ProviderLocation> itr = providerLocations.iterator();
        ProviderLocation providerLoc = itr.next();
        assertEquals("COLUMBUS", providerLoc.getAddress().getCity().toUpperCase());
        
        //NEXT
        providerLoc = itr.next();
        assertEquals("westerville", providerLoc.getAddress().getCity().toLowerCase());
    }
}
