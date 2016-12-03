package org.hni.security.realm.token;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import javax.inject.Inject;

import org.hni.organization.om.Organization;
import org.hni.security.om.UserAccessControls;
import org.hni.security.service.AccessControlService;
import org.hni.user.om.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.IncorrectClaimException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:test-applicationContext.xml"} )
@Transactional
public class TestTokenFactory {

	private static final String KEY = "YbpWo521Z/aF7DqpiIpIHQ==";
	private static final String ISSUER = "test-issuer";
	private static final Long TTL_MILLIS = 3600000L * 3; // 3 hrs
	protected ObjectMapper mapper = new ObjectMapper();
	 
	@Inject private AccessControlService accessControlService;
	
	@Test
	public void testVolunteerToken() throws Exception {
		String subject = "testtoken";
		User user = new User(8L);
		Organization organization = new Organization(3L);
		UserAccessControls acl = accessControlService.getUserAccess(user, organization);
		String token = JWTTokenFactory.encode(KEY, ISSUER, subject, TTL_MILLIS, user.getId(), mapper.writeValueAsString(acl));
		Claims claims = JWTTokenFactory.decode(token, KEY, ISSUER);
		
		UserAccessControls acl2 = mapper.readValue(claims.get("permissions", String.class), UserAccessControls.class);
		assertEquals(1, acl2.getPermissions().size());
		assertEquals("organizations:read:3", acl2.getPermissions().iterator().next());
		assertEquals(1, acl2.getRoles().size());
		assertEquals("3", acl2.getRoles().iterator().next());
		assertEquals(user.getId(), new Long(claims.get("userId", Integer.class).longValue()));
		System.out.println(token);
	}

	@Test
	public void testVolunteerTokenNonMappedOrg() throws Exception {
		String subject = "testtoken";
		User user = new User(8L);
		Organization organization = new Organization(2L);
		UserAccessControls acl = accessControlService.getUserAccess(user, organization);
		String token = JWTTokenFactory.encode(KEY, ISSUER, subject, TTL_MILLIS, user.getId(), mapper.writeValueAsString(acl));
		Claims claims = JWTTokenFactory.decode(token, KEY, ISSUER);
		
		UserAccessControls acl2 = mapper.readValue(claims.get("permissions", String.class), UserAccessControls.class);
		
		//TODO: these should be '0', but due to a change in the security logic for launch
		// we're allowing any organization mapping to fall through.
		assertEquals(1, acl2.getPermissions().size());
		assertEquals(1, acl2.getRoles().size());
		System.out.println(token);
	}

	@Test
	public void testElevatedToken() throws Exception {
		String subject = "test-issuer";
		User user = new User(1L);
		Organization organization = new Organization(2L);
		UserAccessControls acl = accessControlService.getUserAccess(user, organization);
		String token = JWTTokenFactory.encode(KEY, ISSUER, subject, TTL_MILLIS, user.getId(), mapper.writeValueAsString(acl));
		Claims claims = JWTTokenFactory.decode(token, KEY, ISSUER);
		
		UserAccessControls acl2 = mapper.readValue(claims.get("permissions", String.class), UserAccessControls.class);
				
		assertEquals(1, acl2.getRoles().size());
		assertEquals("*:*:*", acl2.getPermissions().iterator().next());
		assertEquals(1, acl2.getPermissions().size());
		assertEquals("1", acl2.getRoles().iterator().next());
		assertEquals(user.getId(), new Long(claims.get("userId", Integer.class).longValue()));
		System.out.println(token);
	}

	@Test(expected=IncorrectClaimException.class)
	public void testInValidToken() {
		String subject = "testtoken";
		Long userId = 123L;
		String permissions = "test permissions";
		String token = JWTTokenFactory.encode(KEY, ISSUER+"-invalid", subject, TTL_MILLIS, userId, permissions);
		JWTTokenFactory.decode(token, KEY, ISSUER);
		fail();
	}

	@Test(expected=ExpiredJwtException.class)
	public void testExpiredToken() throws InterruptedException {
		String subject = "testtoken";
		Long ttlMillis = 1L;
		Long userId = 123L;
		String permissions = "test permissions";
		String token = JWTTokenFactory.encode(KEY, ISSUER, subject, ttlMillis, userId, permissions);
		Thread.sleep(100);
		JWTTokenFactory.decode(token, KEY, ISSUER);
		fail();
	}

}


