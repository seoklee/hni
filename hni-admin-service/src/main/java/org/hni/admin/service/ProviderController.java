package org.hni.admin.service;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.hni.common.Constants;
import org.hni.common.exception.HNIException;
import org.hni.provider.om.Provider;
import org.hni.provider.om.ProviderLocation;
import org.hni.provider.service.ProviderLocationService;
import org.hni.provider.service.ProviderService;
import org.hni.user.dao.AddressDAO;
import org.hni.user.om.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import java.util.Collection;

@Api(value = "/providers", description = "Operations on Providers and ProviderLocations")
@Component
@Path("/providers")
public class ProviderController extends AbstractBaseController {
	private static final Logger logger = LoggerFactory.getLogger(ProviderController.class);
	
	@Inject private ProviderService providerService;
	@Inject private ProviderLocationService providerLocationService;
	@Inject private AddressDAO addressDao;
	
	@GET
	@Path("/{id}")
	@Produces({MediaType.APPLICATION_JSON})
	@ApiOperation(value = "Returns the Provider with the given id"
		, notes = ""
		, response = Provider.class
		, responseContainer = "")
	public Provider getProvider(@PathParam("id") Long id) {
		return providerService.get(id);
	}

	@POST
	@Produces({MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_JSON})
	@ApiOperation(value = "Creates a new Provider or saves the Provider with the given id"
		, notes = "An Provider without an ID field will be created"
		, response = Provider.class
		, responseContainer = "")
	public Provider saveProvider(Provider provider) {
		if (isPermitted(Constants.PROVIDER, Constants.CREATE, 0L)) {
			return providerService.save(provider);
		}
		throw new HNIException("You must have elevated permissions to do this.");
	}

	@DELETE
	@Path("/{id}")
	@Produces({MediaType.APPLICATION_JSON})
	@ApiOperation(value = "Deletes the Provider with the given id"
		, notes = ""
		, response = Provider.class
		, responseContainer = "")
	public Provider getDelete(@PathParam("id") Long id) {
		if (isPermitted(Constants.PROVIDER, Constants.DELETE, id)) {
			return providerService.delete(new Provider(id));
		}
		throw new HNIException("You must have elevated permissions to do this.");
	}

	@POST
	@Path("/{id}/addresses")
	@Produces({MediaType.APPLICATION_JSON})
	@ApiOperation(value = "Adds an address to a Provider"
		, notes = "Use the /addresses API to update addresses"
		, response = Provider.class
		, responseContainer = "")
	public Provider addAddressToProvider(@PathParam("id") Long id, Address address) {
		if (isPermitted(Constants.PROVIDER, Constants.UPDATE, id)) {
			Provider provider = providerService.get(id);
			if (null != provider) {
				provider.setAddress(address);
				providerService.save(provider);
			}
			return provider;
		}
		throw new HNIException("You must have elevated permissions to do this.");
	}

	@DELETE
	@Path("/{id}/addresses/{addressId}")
	@Produces({MediaType.APPLICATION_JSON})
	@ApiOperation(value = "Removes the address from a provider"
		, notes = ""
		, response = Provider.class
		, responseContainer = "")
	public Provider removeAddressFromProvider(@PathParam("id") Long id, @PathParam("addressId") Long addressId) {
		if (isPermitted(Constants.PROVIDER, Constants.DELETE, id)) {
			Provider provider = providerService.get(id);
			if (null != provider) {
				Address address = addressDao.get(addressId);
				if ( null != address ) {
					provider.setAddress(null); // Hibernate will manage the mapping table for us.
					providerService.save(provider);
				}
			}
			return provider;
		}
		throw new HNIException("You must have elevated permissions to do this.");
	}
	
	/** Provider Locations **/

	@GET
	@Path("/{id}/providerLocations")
	@Produces({MediaType.APPLICATION_JSON})
	@ApiOperation(value = "Returns a collection of ProviderLocations for the given Provider"
	, notes = ""
	, response = ProviderLocation.class
	, responseContainer = "")
	public Collection<ProviderLocation> getProviderLocations(@PathParam("id") Long id) {
		return providerLocationService.with(new Provider(id));
	}

	@GET
	@Path("/providerLocations")
	@Produces({MediaType.APPLICATION_JSON})
	@ApiOperation(value = "Returns a collection of nearby ProviderLocations for the given customer address"
			, notes = ""
			, response = ProviderLocation.class
			, responseContainer = "")
	public Collection<ProviderLocation> getProviderLocationsByCustomerAddress(
			@QueryParam("customerId") Long custId,
			@NotNull @QueryParam("address") String customerAddress,
			@DefaultValue("1") @QueryParam("itemsPerPage") int itemsPerPage,
			@DefaultValue("1") @QueryParam("pageNumber") int pageNum,
			@DefaultValue("10") @QueryParam("distance") double distance,
			@DefaultValue("6371.01") @QueryParam("radius") double radius) {
		if (!StringUtils.isBlank(customerAddress)) {
			return providerLocationService.providersNearCustomer(customerAddress, itemsPerPage, distance, radius);
		}
		return null;
	}
	
	@POST
	@Path("/{id}/providerLocations")
	@Produces({MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_JSON})
	@ApiOperation(value = "Adds a ProviderLocation for the given Provider"
	, notes = ""
	, response = ProviderLocation.class
	, responseContainer = "")
	public ProviderLocation addProviderLocation(@PathParam("id") Long id, ProviderLocation providerLocation) {
		if (isPermitted(Constants.PROVIDER, Constants.UPDATE, id)) {
			providerLocation.setProvider(new Provider(id));
			return providerLocationService.save(providerLocation);
		}
		throw new HNIException("You must have elevated permissions to do this.");
	}

	@DELETE
	@Path("/{id}/providerLocations/{plid}")
	@Produces({MediaType.APPLICATION_JSON})
	@ApiOperation(value = "Deletes given ProviderLocation for the given Provider"
	, notes = ""
	, response = ProviderLocation.class
	, responseContainer = "")
	public ProviderLocation addProviderLocation(@PathParam("id") Long id, @PathParam("plid") Long plid) {
		if (isPermitted(Constants.PROVIDER, Constants.DELETE, id)) {
			ProviderLocation providerLocation = providerLocationService.get(plid);
			if (providerLocation.getProvider().getId().equals(id)) {
				providerLocationService.delete(new ProviderLocation(plid));
			}
			//TODO: throw error?
			return null;
		}
		throw new HNIException("You must have elevated permissions to do this.");
	}
	
	@POST
	@Path("/{id}/providerLocations/{plid}/addresses")
	@Produces({MediaType.APPLICATION_JSON})
	@ApiOperation(value = "Adds an address to a ProviderLocation"
		, notes = "Use the /addresses API to update addresses"
		, response = Provider.class
		, responseContainer = "")
	public ProviderLocation addAddressToProviderLocation(@PathParam("id") Long id, @PathParam("plid") Long plid, Address address) {
		if (isPermitted(Constants.PROVIDER, Constants.UPDATE, id)) {
			ProviderLocation providerLocation = providerLocationService.get(plid);
			if (providerLocation.getProvider().getId().equals(id)) {
				providerLocation.setAddress(address);
				providerLocationService.save(providerLocation);
			}
	
			return providerLocation;
		}
		throw new HNIException("You must have elevated permissions to do this.");
	}

	@DELETE
	@Path("/{id}/providerLocations/{plid}/addresses/{addressId}")
	@Produces({MediaType.APPLICATION_JSON})
	@ApiOperation(value = "Removes the address from a providerLocation"
		, notes = ""
		, response = Provider.class
		, responseContainer = "")
	public ProviderLocation removeAddressFromProviderLocation(@PathParam("id") Long id, @PathParam("plid") Long plid, @PathParam("addressId") Long addressId) {
		if (isPermitted(Constants.PROVIDER, Constants.DELETE, id)) {
			ProviderLocation providerLocation = providerLocationService.get(plid);
			if (providerLocation.getProvider().getId().equals(id)) {
				Address address = addressDao.get(addressId);
				if ( null != address ) {
					providerLocation.setAddress(null); // Hibernate will manage the mapping table for us.
					providerLocationService.save(providerLocation);
				}			
			}
	
			return providerLocation;
		}
		throw new HNIException("You must have elevated permissions to do this.");
	}	
}
