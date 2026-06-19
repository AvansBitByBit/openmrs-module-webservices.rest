/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.filter;

import java.io.IOException;
import java.nio.charset.Charset;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.openmrs.util.PrivilegeConstants;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.module.webservices.rest.web.audit.SecurityAuditLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter intended for all /ws/rest calls that allows the user to authenticate via Basic
 * authentication. (It will not fail on invalid or missing credentials. We count on the API to throw
 * exceptions if an unauthenticated user tries to do something they are not allowed to do.) <br/>
 * <br/>
 * IP address authorization is also performed based on the global property:
 * {@link RestConstants#ALLOWED_IPS_GLOBAL_PROPERTY_NAME}
 */
public class AuthorizationFilter implements Filter {
	
	private static final Logger log = LoggerFactory.getLogger(AuthorizationFilter.class);

	private static final int DEFAULT_MAX_FAILURES = 5;

	private static final int DEFAULT_WINDOW_SECONDS = 900;

	private static final int DEFAULT_LOCKOUT_SECONDS = 900;

	private static final AuthenticationRateLimiter RATE_LIMITER = new AuthenticationRateLimiter();
	
	/**
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	@Override
	public void init(FilterConfig arg0) throws ServletException {
		log.debug("Initializing REST WS Authorization filter");
	}
	
	/**
	 * @see javax.servlet.Filter#destroy()
	 */
	@Override
	public void destroy() {
		log.debug("Destroying REST WS Authorization filter");
	}
	
	/**
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse,
	 *      javax.servlet.FilterChain)
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
	        throws IOException, ServletException {
		
		// check the IP address first.  If its not valid, return a 403
		if (!RestUtil.isIpAllowed(request.getRemoteAddr())) {
			// the ip address is not valid, set a 403 http error code
			HttpServletResponse httpresponse = (HttpServletResponse) response;
			SecurityAuditLogger.getInstance().loginFailed(SecurityAuditLogger.UNKNOWN, request.getRemoteAddr(),
			    "IP address not authorized");
			httpresponse.sendError(HttpServletResponse.SC_FORBIDDEN,
			    "IP address '" + request.getRemoteAddr() + "' is not authorized");
			return;
		}
		
		// skip if the session has timed out, we're already authenticated, or it's not an HTTP request
		if (request instanceof HttpServletRequest) {
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			if (httpRequest.getRequestedSessionId() != null && !httpRequest.isRequestedSessionIdValid()) {
				HttpServletResponse httpResponse = (HttpServletResponse) response;
				httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Session timed out");
			}
			
			if (!Context.isAuthenticated()) {
				String basicAuth = httpRequest.getHeader("Authorization");
				if (basicAuth != null) {
					// check that header is in format "Basic ${base64encode(username + ":" + password)}"
					if (basicAuth.startsWith("Basic")) {
						if (requiresSecureTransport() && !httpRequest.isSecure()) {
							HttpServletResponse httpResponse = (HttpServletResponse) response;
							SecurityAuditLogger.getInstance().insecureTransportDenied(SecurityAuditLogger.UNKNOWN,
							    SecurityAuditLogger.where(httpRequest), "Basic authentication requires HTTPS");
							httpResponse.sendError(426, "HTTPS is required for REST authentication");
							return;
						}
						try {
							// remove the leading "Basic "
							basicAuth = basicAuth.substring(6);
							if (StringUtils.isBlank(basicAuth)) {
								HttpServletResponse httpResponse = (HttpServletResponse) response;
								SecurityAuditLogger.getInstance().loginFailed(SecurityAuditLogger.UNKNOWN,
								    SecurityAuditLogger.where(httpRequest), "Invalid authorization header");
								httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid credentials provided");
								return;
							}
							
							String decoded = new String(Base64.decodeBase64(basicAuth), Charset.forName("UTF-8"));
							if (StringUtils.isBlank(decoded) || !decoded.contains(":")) {
								HttpServletResponse httpResponse = (HttpServletResponse) response;
								SecurityAuditLogger.getInstance().loginFailed(SecurityAuditLogger.UNKNOWN,
								    SecurityAuditLogger.where(httpRequest), "Invalid authorization header");
								httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid credentials provided");
								return;
							}
							
							String[] userAndPass = decoded.split(":", 2);
							String username = userAndPass[0];
							if (isRateLimitEnabled() && RATE_LIMITER.isLocked(username, request.getRemoteAddr(), getMaxFailures(),
							    getWindowMillis(), getLockoutMillis())) {
								HttpServletResponse httpResponse = (HttpServletResponse) response;
								SecurityAuditLogger.getInstance().loginLockoutDenied(username,
								    SecurityAuditLogger.where(httpRequest), "Authentication temporarily locked");
								httpResponse.sendError(429, "Too many failed authentication attempts");
								return;
							}
							Context.authenticate(userAndPass[0], userAndPass[1]);
							RATE_LIMITER.recordSuccess(username, request.getRemoteAddr());
							SecurityAuditLogger.getInstance().loginSucceeded(username,
							    SecurityAuditLogger.where(httpRequest), "Basic authentication accepted");
						}
						catch (Exception ex) {
							String username = SecurityAuditLogger.UNKNOWN;
							try {
								String decoded = new String(Base64.decodeBase64(basicAuth), Charset.forName("UTF-8"));
								if (decoded.contains(":")) {
									username = decoded.split(":", 2)[0];
								}
							}
							catch (RuntimeException ignored) {
							}
							SecurityAuditLogger.getInstance().loginFailed(username, SecurityAuditLogger.where(httpRequest),
							    "Invalid credentials");
							if (isRateLimitEnabled()
							        && RATE_LIMITER.recordFailure(username, request.getRemoteAddr(), getMaxFailures(), getWindowMillis(),
							            getLockoutMillis())) {
								SecurityAuditLogger.getInstance().loginLockout(username, SecurityAuditLogger.where(httpRequest),
								    "Authentication temporarily locked after repeated failures");
							}
							HttpServletResponse httpResponse = (HttpServletResponse) response;
							httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid credentials");
							return;
						}
					}
				}
			}
		}
		
		// continue with the filter chain (unless IP is not allowed)
		chain.doFilter(request, response);
	}

	static void resetRateLimiter() {
		RATE_LIMITER.reset();
	}

	private boolean requiresSecureTransport() {
		return getBooleanGlobalProperty(RestConstants.REQUIRE_SECURE_TRANSPORT_GLOBAL_PROPERTY_NAME, true);
	}

	private boolean isRateLimitEnabled() {
		return getBooleanGlobalProperty(RestConstants.AUTH_RATE_LIMIT_ENABLED_GLOBAL_PROPERTY_NAME, true);
	}

	private int getMaxFailures() {
		return getIntegerGlobalProperty(RestConstants.AUTH_RATE_LIMIT_MAX_FAILURES_GLOBAL_PROPERTY_NAME, DEFAULT_MAX_FAILURES);
	}

	private long getWindowMillis() {
		return getIntegerGlobalProperty(RestConstants.AUTH_RATE_LIMIT_WINDOW_SECONDS_GLOBAL_PROPERTY_NAME, DEFAULT_WINDOW_SECONDS) * 1000L;
	}

	private long getLockoutMillis() {
		return getIntegerGlobalProperty(RestConstants.AUTH_RATE_LIMIT_LOCKOUT_SECONDS_GLOBAL_PROPERTY_NAME, DEFAULT_LOCKOUT_SECONDS) * 1000L;
	}

	private boolean getBooleanGlobalProperty(String propertyName, boolean defaultValue) {
		return Boolean.parseBoolean(getGlobalProperty(propertyName, Boolean.toString(defaultValue)));
	}

	private int getIntegerGlobalProperty(String propertyName, int defaultValue) {
		try {
			return Integer.parseInt(getGlobalProperty(propertyName, Integer.toString(defaultValue)));
		}
		catch (NumberFormatException ex) {
			return defaultValue;
		}
	}

	private String getGlobalProperty(String propertyName, String defaultValue) {
		try {
			Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
			return Context.getAdministrationService().getGlobalProperty(propertyName, defaultValue);
		}
		catch (RuntimeException ex) {
			return defaultValue;
		}
		finally {
			Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
		}
	}
}
