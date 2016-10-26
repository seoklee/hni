package org.hni.admin.service;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.hni.common.Constants;
import org.hni.om.Role;
import org.hni.organization.om.Organization;
import org.hni.organization.om.UserOrganizationRole;
import org.hni.organization.service.OrganizationUserService;
import org.hni.user.om.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "/user", description = "Operations on Users and to manage Users relationships to organiations")
@Component
@Path("/user")
public class UserServiceController {
	private static final Logger logger = LoggerFactory.getLogger(UserServiceController.class);
	
	@Inject private OrganizationUserService orgUserService;
	
	@GET
	@Path("/{id}")
	@Produces({MediaType.APPLICATION_JSON})
	@ApiOperation(value = "Returns the user with the given id"
	, notes = ""
	, response = User.class
	, responseContainer = "")
	public User getUser(@PathParam("id") Long id) {
		return orgUserService.get(id);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({MediaType.APPLICATION_JSON})
	@ApiOperation(value = "Creates a new user and returns it"
	, notes = ""
	, response = User.class
	, responseContainer = "")
	public User addOrSaveUser(User user) {
		return orgUserService.save(user);
	}

	@DELETE
	@Path("/{id}/org/{orgId}/role/{roleId}")
	@Produces({MediaType.APPLICATION_JSON})
	@ApiOperation(value = "Removes a user's role from the given organization"
	, notes = ""
	, response = User.class
	, responseContainer = "")
	public String deleteUser(@PathParam("id") Long id, @PathParam("orgId") Long orgId, @PathParam("roleId") Long roleId) {
		User user = new User(id);
		Organization org = new Organization(orgId);
		orgUserService.delete(user, org, Role.get(roleId));
		return "OK";
	}

	@PUT
	@Path("/{id}/org/{orgId}/role/{roleId}")
	@Produces({MediaType.APPLICATION_JSON})
	@ApiOperation(value = "Adds the user to an organization with a specific role"
	, notes = ""
	, response = UserOrganizationRole.class
	, responseContainer = "")
	public UserOrganizationRole addUserToOrg(@PathParam("id") Long id, @PathParam("orgId") Long orgId, @PathParam("roleId") Long roleId) {
		User user = new User(id);
		Organization org = new Organization(orgId);
		return orgUserService.associate(user, org, Role.get(roleId));
	}
	
	@GET
	@Path("/users/org/{id}")
	@Produces({MediaType.APPLICATION_JSON})
	@ApiOperation(value = "Returns a collection of users for the given organization with the USER role."
	, notes = ""
	, response = User.class
	, responseContainer = "")
	public Collection<User> getOrgUsers(@PathParam("id") Long id) {
		Organization org = new Organization(id);
		return orgUserService.getAllUsers(org);
	}
	
	@GET
	@Path("/clients/org/{id}")
	@Produces({MediaType.APPLICATION_JSON})
	@ApiOperation(value = "Returns a collection of users for the given organization with the CLIENT role."
	, notes = ""
	, response = User.class
	, responseContainer = "")
	public Collection<User> getOrgClients(@PathParam("id") Long id) {
		Organization org = new Organization(id);
		return orgUserService.getByRole(org, Role.get(Constants.CLIENT));
	}
	
	
}
