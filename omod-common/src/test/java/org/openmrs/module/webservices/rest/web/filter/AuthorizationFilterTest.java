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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class AuthorizationFilterTest extends BaseModuleWebContextSensitiveTest {
	
	private AuthorizationFilter filter;
	
	@Before
	public void setUp() {
		filter = new AuthorizationFilter();
		AuthorizationFilter.resetRateLimiter();
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(RestConstants.ALLOWED_IPS_GLOBAL_PROPERTY_NAME, "127.0.0.1"));
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(RestConstants.AUTH_RATE_LIMIT_ENABLED_GLOBAL_PROPERTY_NAME, "true"));
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(RestConstants.AUTH_RATE_LIMIT_MAX_FAILURES_GLOBAL_PROPERTY_NAME, "2"));
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(RestConstants.AUTH_RATE_LIMIT_WINDOW_SECONDS_GLOBAL_PROPERTY_NAME, "900"));
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(RestConstants.AUTH_RATE_LIMIT_LOCKOUT_SECONDS_GLOBAL_PROPERTY_NAME, "900"));
		Context.logout();
	}
	
	@Test
	public void doFilter_shouldDenyBasicAuthOverInsecureTransportIfRequired() throws Exception {
		Context.authenticate("admin", "test");
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(RestConstants.REQUIRE_SECURE_TRANSPORT_GLOBAL_PROPERTY_NAME, "true"));
		Context.logout();
		MockHttpServletRequest request = request("admin", "test");
		request.setSecure(false);
		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain chain = mock(FilterChain.class);
		
		filter.doFilter(request, response, chain);
		
		org.junit.Assert.assertEquals(426, response.getStatus());
		verify(chain, never()).doFilter(request, response);
	}
	
	@Test
	public void doFilter_shouldAllowBasicAuthOverInsecureTransportIfExplicitlyDisabled() throws Exception {
		Context.authenticate("admin", "test");
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(RestConstants.REQUIRE_SECURE_TRANSPORT_GLOBAL_PROPERTY_NAME, "false"));
		Context.logout();
		MockHttpServletRequest request = request("admin", "test");
		request.setSecure(false);
		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain chain = mock(FilterChain.class);
		
		filter.doFilter(request, response, chain);
		
		org.junit.Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
		verify(chain).doFilter(request, response);
	}
	
	@Test
	public void doFilter_shouldStopInvalidBasicAuthWithUnauthorized() throws Exception {
		Context.authenticate("admin", "test");
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(RestConstants.REQUIRE_SECURE_TRANSPORT_GLOBAL_PROPERTY_NAME, "false"));
		Context.logout();
		MockHttpServletRequest request = request("admin", "wrong");
		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain chain = mock(FilterChain.class);
		
		filter.doFilter(request, response, chain);
		
		org.junit.Assert.assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
		verify(chain, never()).doFilter(request, response);
	}
	
	@Test
	public void doFilter_shouldLockOutAfterConfiguredFailures() throws Exception {
		Context.authenticate("admin", "test");
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(RestConstants.REQUIRE_SECURE_TRANSPORT_GLOBAL_PROPERTY_NAME, "false"));
		Context.logout();
		FilterChain chain = mock(FilterChain.class);
		
		filter.doFilter(request("admin", "wrong"), new MockHttpServletResponse(), chain);
		filter.doFilter(request("admin", "wrong-again"), new MockHttpServletResponse(), chain);
		MockHttpServletRequest lockedRequest = request("admin", "wrong-third");
		MockHttpServletResponse lockedResponse = new MockHttpServletResponse();
		
		filter.doFilter(lockedRequest, lockedResponse, chain);
		
		org.junit.Assert.assertEquals(429, lockedResponse.getStatus());
		verify(chain, never()).doFilter(lockedRequest, lockedResponse);
	}
	
	@Test
	public void doFilter_shouldResetFailuresAfterSuccessfulLogin() throws Exception {
		Context.authenticate("admin", "test");
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(RestConstants.REQUIRE_SECURE_TRANSPORT_GLOBAL_PROPERTY_NAME, "false"));
		Context.logout();
		FilterChain chain = mock(FilterChain.class);
		
		filter.doFilter(request("admin", "wrong"), new MockHttpServletResponse(), chain);
		filter.doFilter(request("admin", "test"), new MockHttpServletResponse(), chain);
		Context.logout();
		MockHttpServletResponse secondFailureAfterReset = new MockHttpServletResponse();
		
		filter.doFilter(request("admin", "wrong-again"), new MockHttpServletResponse(), chain);
		filter.doFilter(request("admin", "wrong-third"), secondFailureAfterReset, chain);
		
		org.junit.Assert.assertEquals(HttpServletResponse.SC_UNAUTHORIZED, secondFailureAfterReset.getStatus());
	}
	
	private MockHttpServletRequest request(String username, String password) {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/openmrs/ws/rest/v1/session");
		request.setRemoteAddr("127.0.0.1");
		request.addHeader("Authorization", "Basic " + Base64.encodeBase64String((username + ":" + password)
		        .getBytes(StandardCharsets.UTF_8)));
		return request;
	}
}
