package org.hni.security.service;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.hni.common.om.Role;
import org.hni.organization.om.Organization;
import org.hni.organization.om.UserOrganizationRole;
import org.hni.organization.service.OrganizationService;
import org.hni.organization.service.OrganizationUserService;
import org.hni.security.dao.PermissionDAO;
import org.hni.security.dao.RoleDAO;
import org.hni.security.dao.RolePermissionDAO;
import org.hni.security.om.Permission;
import org.hni.security.om.RolePermission;
import org.hni.security.om.UserAccessControls;
import org.hni.user.om.User;
import org.springframework.stereotype.Component;

@Component
public class AccessControlService {

	private OrganizationUserService orgUserService;
	private RolePermissionDAO rolePermissionDao;
	private RoleDAO roleDao;
	private PermissionDAO permissionDao;
	private OrganizationService organizationService;
	
	@Inject
	public AccessControlService(OrganizationUserService orgUserService, RolePermissionDAO rolePermissionDao, RoleDAO roleDao, PermissionDAO permissionDao, OrganizationService organizationService) {
		this.orgUserService = orgUserService;
		this.rolePermissionDao = rolePermissionDao;
		this.roleDao = roleDao;
		this.permissionDao = permissionDao;
		this.organizationService = organizationService;
	}
	
	public UserAccessControls getUserAccess(User user, Organization organization) {
		UserAccessControls userAccess = new UserAccessControls();
		
		Collection<UserOrganizationRole> userOrganizationRoles = orgUserService.getUserOrganizationRoles(user);
		for(UserOrganizationRole uor : userOrganizationRoles) {
			//if (uor.getId().getOrgId().equals(organization.getId())) {
				Role role = roleDao.get(uor.getId().getRoleId());		
				Organization org = organizationService.get(uor.getId().getOrgId());
				if ( null == org ) { // hack
					org = organization;
				}
				//userAccess.getRoles().add(role.getName());
				userAccess.getRoles().add(role.getId().toString()); // UI wants ID's instead
				// now get the map of permissions to this role
				List<RolePermission> rolePermissions = rolePermissionDao.byRole(uor.getId().getRoleId());
				for(RolePermission rp : rolePermissions) {
					Permission p = permissionDao.get(rp.getId().getPermissionId());
					if (p.getDomain().equals("organizations") || p.getDomain().equals("*")) {
						userAccess.getPermissions().add(createPermission(p.getDomain(), p.getValue(), org.getId(), rp.isAllInstances()));
					}
				}
			//}
		}
		return userAccess;
	}
	
	private String createPermission(String domain, String permission, Long instanceId, boolean allInstances) {
		String instance = (allInstances == true)?"*":instanceId.toString();
		return String.format("%s:%s:%s", domain, permission, instance);
	}
}
