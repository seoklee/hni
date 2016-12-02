package org.hni.admin.service;

import java.util.Collection;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.util.ThreadContext;
import org.hni.common.Constants;
import org.hni.common.exception.HNIException;
import org.hni.common.om.Role;
import org.hni.organization.om.Organization;
import org.hni.organization.om.UserOrganizationRole;
import org.hni.organization.service.OrganizationUserService;
import org.hni.security.dao.RoleDAO;
import org.hni.user.om.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "/users", description = "Operations on Users and to manage Users relationships to organiations")
@Component
@Path("/users")
public class UserServiceController extends AbstractBaseController {
	private static final Logger logger = LoggerFactory.getLogger(UserServiceController.class);
	
	@Inject private OrganizationUserService orgUserService;
	@Inject private RoleDAO roleDao;	
    @Context private HttpServletRequest servletRequest;
    
	@GET
	@Path("/{id}")
	@Produces({MediaType.APPLICATION_JSON})
	@ApiOperation(value = "Returns the user with the given id"
	, notes = ""
	, response = User.class
	, responseContainer = "")
	public Response getUser(@PathParam("id") Long id) {
		//return orgUserService.get(id);
		return Response.ok(orgUserService.get(id), MediaType.APPLICATION_JSON).build();
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({MediaType.APPLICATION_JSON})
	@ApiOperation(value = "Creates a new user or updates an existing one and returns it"
	, notes = "An update occurs if the ID field is specified"
	, response = User.class
	, responseContainer = "")
	public User addOrSaveUser(User user) {
		if (isPermitted(Constants.ORGANIZATION, Constants.CREATE, 0L)) {
			return orgUserService.save(user);
		}
		throw new HNIException("You must have elevated permissions to do this.");
	}

	@DELETE
	@Path("/{id}/organizations/{orgId}/roles/{roleId}")
	@Produces({MediaType.APPLICATION_JSON})
	@ApiOperation(value = "Removes a user's role from the given organization"
	, notes = ""
	, response = User.class
	, responseContainer = "")
	public String deleteUser(@PathParam("id") Long id, @PathParam("orgId") Long orgId, @PathParam("roleId") Long roleId) {
		if (isPermitted(Constants.ORGANIZATION, Constants.DELETE, id)) {
			User user = new User(id);
			Organization org = new Organization(orgId);
			Role role = Role.get(roleId);
			orgUserService.delete(user, org, Role.get(roleId));
			return "OK";
		}
		throw new HNIException("You must have elevated permissions to do this.");
	}

	@PUT
	@Path("/{id}/organizations/{orgId}/roles/{roleId}")
	@Produces({MediaType.APPLICATION_JSON})
	@ApiOperation(value = "Adds the user to an organization with a specific role"
	, notes = ""
	, response = UserOrganizationRole.class
	, responseContainer = "")
	public UserOrganizationRole addUserToOrg(@PathParam("id") Long id, @PathParam("orgId") Long orgId, @PathParam("roleId") Long roleId) {
		if (isPermitted(Constants.ORGANIZATION, Constants.UPDATE, id)) {
			User user = new User(id);
			Organization org = new Organization(orgId);
			Role role = Role.get(roleId);
			if ( null != user && null != org && null != role) {
				return orgUserService.associate(user, org, Role.get(roleId));
			}
			throw new HNIException("One of the ID's you sent was invalid...");
		}
		throw new HNIException("You must have elevated permissions to do this.");
	}
	
	@GET
	@Path("/organizations/{orgId}/roles/{roleId}")
	@Produces({MediaType.APPLICATION_JSON})
	@ApiOperation(value = "Returns a collection of users for the given organization with the given roleId."
	, notes = ""
	, response = User.class
	, responseContainer = "")
	public Collection<User> getOrgUsers(@PathParam("orgId") Long orgId, @QueryParam("roleId") Long roleId) {
		Organization org = new Organization(orgId);
		return orgUserService.getByRole(org, Role.get(roleId));
	}

	@GET
	@Path("/roles")
	@Produces({MediaType.APPLICATION_JSON})
	@ApiOperation(value = "Returns a collection of all potential roles for users in the system."
	, notes = ""
	, response = User.class
	, responseContainer = "")
	public Collection<Role> getUserRoles() {
		
		return roleDao.getAll();
	}

	@DELETE
	@Path("/{id}/organizations/{orgId}")
	@Produces({MediaType.APPLICATION_JSON})
	@ApiOperation(value = "Removes a user from the given organization"
	, notes = ""
	, response = User.class
	, responseContainer = "")
	public String deleteUserFromOrg(@PathParam("id") Long id, @PathParam("orgId") Long orgId) {
		if (isPermitted(Constants.ORGANIZATION, Constants.DELETE, id)) {
			User user = new User(id);
			Organization org = new Organization(orgId);
			orgUserService.delete(user, org);
			return "OK";
		}
		throw new HNIException("You must have elevated permissions to do this.");
	}

	@GET
	@Path("/userinfo")
	@Produces({MediaType.APPLICATION_JSON})
	@ApiOperation(value = "Returns info about the user in the current thread context"
	, notes = ""
	, response = User.class
	, responseContainer = "")
	public User getUser() {
		Long userId = (Long)ThreadContext.get(Constants.USERID); // this was placed onto the context by the JWTTokenAuthenticatingFilter
		return orgUserService.get(userId);
	}

	@GET
	@Path("/organizations")
	@Produces({MediaType.APPLICATION_JSON})
	@ApiOperation(value = "All users for all organizations..yikes!"
	, notes = ""
	, response = User.class
	, responseContainer = "")
	public Collection<User> getUsersByRole(@QueryParam("roleId") Long roleId) {
		if (SecurityUtils.getSubject().hasRole(Constants.SUPER_USER.toString())) {		
			return orgUserService.byRole(Role.get(roleId));
		}
		throw new HNIException("You must have elevated permissions to do this.");
	}

}
