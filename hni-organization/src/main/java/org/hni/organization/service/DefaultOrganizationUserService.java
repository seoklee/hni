package org.hni.organization.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.hni.common.Constants;
import org.hni.common.om.Role;
import org.hni.organization.dao.UserOrganizationRoleDAO;
import org.hni.organization.om.Organization;
import org.hni.organization.om.UserOrganizationRole;
import org.hni.organization.om.UserOrganizationRolePK;
import org.hni.user.dao.UserDAO;
import org.hni.user.om.User;
import org.hni.user.service.DefaultUserService;
import org.springframework.stereotype.Component;

@Component("orgUserService")
public class DefaultOrganizationUserService extends DefaultUserService implements OrganizationUserService {

	public static final Long USER = 5L;

	private OrganizationService orgService;
	private UserOrganizationRoleDAO uorDao;

	@Inject
	public DefaultOrganizationUserService(UserDAO userDao, OrganizationService orgService, UserOrganizationRoleDAO uorDao) {
		super(userDao);
		this.orgService = orgService;
		this.uorDao = uorDao;
	}

    @Override
    public User save(User user, Organization org, Role role) {
        super.save(user);
        UserOrganizationRole uor = new UserOrganizationRole(user, org, role);
        uorDao.save(uor);
        return user;
    }

	@Override
	public Collection<User> getByRole(Organization org, Role role) {
		Set<User> set = new HashSet<>();
		Collection<UserOrganizationRole> userRolesList = uorDao.getByRole(org, role);
		for (UserOrganizationRole uor : userRolesList) {
			set.add(super.get(((UserOrganizationRolePK) uor.getId()).getUserId()));
		}
		return new ArrayList<User>(set);
	}

	@Override
	public UserOrganizationRole associate(User user, Organization org, Role role) {
		return uorDao.save(new UserOrganizationRole(user, org, role));
	}

	@Override
	public Collection<User> getAllUsers(Organization org) {
		return getByRole(org, Role.get(USER));
	}

	@Override
	public void delete(User user, Organization org) {
		for (UserOrganizationRole uor : uorDao.get(user)) {
			uorDao.delete(uor);
		}
	}

	@Override
	public void delete(User user, Organization org, Role role) {
		uorDao.delete(new UserOrganizationRole(user, org, role));
	}

	@Override
	public void lock(User user) {
		for(UserOrganizationRole uor : uorDao.get(user)) {
			uorDao.delete(uor);
		}
		user.setDeleted(true);
		user.setHashedSecret("LOCKED");
		save(user);
	}
	
	@Override
	public User archive(User user, Organization org) {
		// TODO
		return user;
	}

	@Override
	public Collection<Organization> get(User user) {
		Collection<Organization> orgs = new HashSet<>();
		for (UserOrganizationRole uor : uorDao.get(user)) {
			orgs.add(orgService.get(uor.getId().getOrgId()));
		}
		return orgs;
	}

	@Override
	public Collection<UserOrganizationRole> getUserOrganizationRoles(User user) {
		return uorDao.get(user);
	}

	@Override
	public Collection<User> byRole(Role role) {
		Collection<User> users = new HashSet<User>();
		for (UserOrganizationRole uor : uorDao.byRole(role)) {
			users.add(this.get(uor.getId().getUserId()));			
		}
		
		return users;
	}

}
