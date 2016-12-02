package org.hni.admin.service;

import java.util.Collection;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.hni.common.Constants;
import org.hni.common.exception.HNIException;
import org.hni.organization.om.Organization;
import org.hni.organization.service.OrganizationService;
import org.hni.organization.service.OrganizationUserService;
import org.hni.user.om.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "/organizations", description = "Operations on Organizations")
@Component
@Path("/organizations")
public class OrganizationServiceController extends AbstractBaseController {
	private static final Logger logger = LoggerFactory.getLogger(OrganizationServiceController.class);
	
	@Inject private OrganizationService orgService;
	@Inject private OrganizationUserService orgUserService;
	
	@GET
	@Path("/{id}")
	@Produces({MediaType.APPLICATION_JSON})
	@ApiOperation(value = "Returns the organization with the given id"
	, notes = ""
	, response = Organization.class
	, responseContainer = "")
	public Organization getUser(@PathParam("id") Long id) {
		return orgService.get(id);
	}

	@POST
	@Produces({MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_JSON})
	@ApiOperation(value = "Creates or updates an organization with the given id"
	, notes = "An update occurs if the ID field is provided"
	, response = Organization.class
	, responseContainer = "")
	public Organization saveOrUpdate(Organization org) {
		if (isPermitted(Constants.ORGANIZATION, Constants.CREATE, 0L)) {
			return orgService.save(org);
		}
		throw new HNIException("You must have elevated permissions to do this.");
	}

	@DELETE
	@Path("/{id}")
	@Produces({MediaType.APPLICATION_JSON})
	@ApiOperation(value = "Deletes the organization with the given id"
	, notes = ""
	, response = Organization.class
	, responseContainer = "")
	public Organization deleteOrganization(@PathParam("id") Long id) {
		if (isPermitted(Constants.ORGANIZATION, Constants.DELETE, 0L)) {
			return orgService.delete(new Organization(id));
		}
		throw new HNIException("You must have elevated permissions to do this.");
	}
	
	@GET
	@Path("/users/{id}")
	@Produces({MediaType.APPLICATION_JSON})
	@ApiOperation(value = "Returns the organizations that the given user is related to"
	, notes = ""
	, response = Organization.class
	, responseContainer = "")
	public Collection<Organization> getAllForUser(@PathParam("id") Long id) {
		User user = new User(id);
		return orgUserService.get(user);
	}

	@GET
	@Path("/all")
	@Produces({MediaType.APPLICATION_JSON})
	@ApiOperation(value = "Returns all organizations"
	, notes = ""
	, response = Organization.class
	, responseContainer = "")
	public Collection<Organization> getAll() {
		return orgService.getAll();
	}

}
