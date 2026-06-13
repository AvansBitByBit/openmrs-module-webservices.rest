/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.controller;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.docs.swagger.SwaggerSpecificationCreator;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;

public class SwaggerSpecificationControllerTest extends BaseModuleWebContextSensitiveTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	private SwaggerSpecificationController controller;

	@Before
	public void before() {
		controller = new SwaggerSpecificationController();
		if (SwaggerSpecificationCreator.isCached()) {
			SwaggerSpecificationCreator.clearCache();
		}
		Context.getService(RestService.class).initialize();
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(RestConstants.SWAGGER_QUIET_DOCS_GLOBAL_PROPERTY_NAME, "false"));
	}

	@Test
	public void getSwaggerSpecification_shouldUseRequestHostSchemeAndContextPath() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/module/webservices/rest/swagger.json");
		request.addHeader("Host", "openmrs.example.org");
		request.setScheme("http");
		request.setContextPath("/openmrs");

		Map<String, Object> swagger = readSwagger(controller.getSwaggerSpecification(request));

		Assert.assertEquals("openmrs.example.org", swagger.get("host"));
		Assert.assertEquals("/openmrs/ws/rest/v1", swagger.get("basePath"));
		Assert.assertTrue(((List) swagger.get("schemes")).contains("http"));
	}

	@Test
	public void getSwaggerSpecification_shouldUseForwardedProtocolIfPresent() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/module/webservices/rest/swagger.json");
		request.addHeader("Host", "openmrs.example.org");
		request.addHeader("X-Forwarded-Proto", "https");
		request.setScheme("http");
		request.setContextPath("/openmrs");

		Map<String, Object> swagger = readSwagger(controller.getSwaggerSpecification(request));

		Assert.assertTrue(((List) swagger.get("schemes")).contains("https"));
		Assert.assertFalse(((List) swagger.get("schemes")).contains("http"));
	}

	private Map<String, Object> readSwagger(String json) throws Exception {
		return objectMapper.readValue(json, Map.class);
	}
}
