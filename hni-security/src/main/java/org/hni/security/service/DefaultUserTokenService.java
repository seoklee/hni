package org.hni.security.service;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import org.hni.common.Constants;
import org.hni.organization.om.UserOrganizationRole;
import org.hni.organization.service.OrganizationUserService;
import org.hni.security.om.OrganizationUserRolePermission;
import org.hni.security.om.Permission;
import org.hni.security.om.UserAccessControls;
import org.hni.security.realm.token.JWTTokenFactory;
import org.hni.user.om.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;

@Component
public class DefaultUserTokenService implements UserTokenService {
	private static final Logger logger = LoggerFactory.getLogger(DefaultUserTokenService.class);
	
	@Inject	private OrganizationUserService orgUserService;
	@Inject	private RolePermissionService rolePermissionService;
	@Value("#{hniProperties['token.issuer']}") private String tokenIssuer;
	@Value("#{hniProperties['token.key']}") private String tokenKey;

	private static final ObjectMapper objectMapper = new ObjectMapper();

	public Claims getClaimsFromToken(String token) {
		return JWTTokenFactory.decode(token, tokenKey, tokenIssuer);
	}

	public Long getUserIdFromClaims(Claims claims) {
		Long userId = new Long(claims.get(Constants.USERID, Integer.class).longValue());
		return userId;
	}

	public UserAccessControls getPermissionsFromClaims(Claims claims) {
		  
		
		String permissionsString = claims.get(Constants.PERMISSIONS, String.class);
		UserAccessControls acl;
		try {
			acl = objectMapper.readValue(permissionsString, UserAccessControls.class);
			return acl;
		} catch (IOException e) {
			logger.warn("Unable to desesrialize access controls from Token...return blank ACL");
			return new UserAccessControls();
		}
	}

	public Set<OrganizationUserRolePermission> getUserOrganizationRolePermissions(User user, Long organizationId) {
		Set<UserOrganizationRole> userOrgRoles = new TreeSet<UserOrganizationRole>();
		Collection<UserOrganizationRole> userOrganizationRoles = orgUserService.getUserOrganizationRoles(user);
		organizationId = validateAndAssignOrganizationId(organizationId, userOrganizationRoles);
		for (UserOrganizationRole userOrgRole : userOrganizationRoles) {
			if (organizationId.equals(userOrgRole.getId().getOrgId())) {
				userOrgRoles.add(userOrgRole);
			}
		}
		Set<OrganizationUserRolePermission> orgPermissions = new TreeSet<OrganizationUserRolePermission>();
		for (UserOrganizationRole userOrgRole : userOrgRoles) {
			OrganizationUserRolePermission orgUserRolePermission = new OrganizationUserRolePermission();
			orgUserRolePermission.setOrganizationId(organizationId);
			orgUserRolePermission.setRoleId(userOrgRole.getId().getRoleId());
			orgUserRolePermission.setUserId(user.getId());
			Set<Permission> permissions = rolePermissionService.byRoleId(userOrgRole.getId().getRoleId(), userOrgRole.getId().getOrgId());
			orgUserRolePermission.setPermissions(permissions);
			orgPermissions.add(orgUserRolePermission);
		}
		return orgPermissions;
	}

	@Override
	public UserAccessControls getPermissionsFromToken(String token) {
		Claims claims = getClaimsFromToken(token);
		return getPermissionsFromClaims(claims);
	}
	
	private Long validateAndAssignOrganizationId(Long organizationId, Collection<UserOrganizationRole> userOrganizationRoles) {
		if ( null == organizationId && userOrganizationRoles.size() > 0) {
			// find an organization to use
			UserOrganizationRole[] array = userOrganizationRoles.toArray(new UserOrganizationRole[userOrganizationRoles.size()]);
			organizationId = array[0].getId().getOrgId();			
		}
		return organizationId;
	}
}
