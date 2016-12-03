package org.hni.admin.filter;

import java.io.IOException;
import java.util.Enumeration;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.hni.common.Constants;
import org.hni.security.realm.token.JWTAuthenticationToken;
import org.hni.security.realm.token.JWTTokenFactory;
import org.hni.security.service.UserTokenService;
import org.hni.user.dao.UserDAO;
import org.hni.user.om.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.MissingClaimException;

/**
 * This filter is bound to all service calls requiring them all to present a
 * valid JWT token before they can proceed.
 * 
 * @author j2parke
 *
 */
public class JWTTokenAuthenticatingFilter extends AuthenticatingFilter {
	private static final Logger logger = LoggerFactory.getLogger(JWTTokenAuthenticatingFilter.class);
	public static final String TOKEN_HEADER = "x-hni-token";
	public static final String USER_AGENT = "user-agent";
	
	private String tokenIssuer;
	private String tokenKey;
	
	@Inject
	private UserDAO userDao;

	public void setTokenIssuer(String tokenIssuer) {
		this.tokenIssuer = tokenIssuer;
	}
	public void setTokenKey(String tokenKey) {
		this.tokenKey = tokenKey;
	}
	
	@Override
	protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String tokenValue = httpRequest.getHeader(TOKEN_HEADER);
		String userAgent = httpRequest.getHeader(USER_AGENT);
		
		try {
			// if the token is valid we'll put the claims onto the ThreadLocal
			// for processing by the TokenRealm
			Claims claims = JWTTokenFactory.decode(tokenValue, tokenKey, tokenIssuer);
			Long userId = new Long(claims.get(Constants.USERID, Integer.class).longValue());

			// place the pre-calc permissions onto the Thread.local so we don't have to calc them again
			ThreadContext.put(Constants.PERMISSIONS, claims.get(Constants.PERMISSIONS, String.class));
			ThreadContext.put(Constants.USERID, userId);

			User user = userDao.get(userId);
			if (null != user) {
				logger.info(String.format("User[%d] %s %s, %s - User-Agent:%s ", user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), userAgent));
				logger.info(String.format("Found user %s for the token.  Authenticating...", user.getEmail()));
				return new JWTAuthenticationToken(user.getEmail(), userId);
			}
			logger.error("token authenication failed...no user");
			//throw new WebApplicationException("auth-token is missing, invalid or expired: user couldn't be validated", Response.Status.FORBIDDEN);
			throw new AuthenticationException("the token is invalid; no user found");
		} catch (MissingClaimException | IncorrectClaimException | ExpiredJwtException e) {
			// let this fall through so the authN fails
			logger.info("token issuer = "+tokenIssuer);
			logger.error("      token = " + tokenValue);
			logger.error("Not able to validate token due to " + e.getMessage());
			throw new AuthenticationException("auth-token is missing, invalid or expired");
		}
		
		
		//return new JWTAuthenticationToken("", null);
	}

	@Override
	protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
		// force the token auth on every request
		return false;
	}

	@Override
	protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
		boolean loggedIn = executeLogin(request, response);
		if (!loggedIn) {
			HttpServletResponse httpResponse = (HttpServletResponse)response;
			httpResponse.setContentType(MediaType.APPLICATION_JSON);
			httpResponse.getOutputStream().write(String.format("{\"message\":\"%s\"}","Unable to authenticate using the token or credentials provided").getBytes());
			httpResponse.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
			}
		return loggedIn;
	}

	@Override
	protected void cleanup(ServletRequest request, ServletResponse response, Exception existing) throws ServletException, IOException {

		HttpServletResponse httpResponse = (HttpServletResponse)response;
		if ( null != existing ) {
			httpResponse.setContentType(MediaType.APPLICATION_JSON);
			httpResponse.getOutputStream().write(String.format("{\"message\":\"%s\"}", existing.getMessage()).getBytes());
			httpResponse.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
			existing = null; // prevent Shiro from tossing a ServletException
		}
		super.cleanup(request, httpResponse, existing);
	}
}
