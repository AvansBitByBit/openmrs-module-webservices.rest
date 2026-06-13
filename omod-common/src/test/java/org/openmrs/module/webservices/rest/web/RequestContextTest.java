/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;

public class RequestContextTest extends BaseModuleWebContextSensitiveTest {

	/**
	 * @see RequestContext#setLimit(Integer)
	 * @verifies not accept a value less than one
	 */
	@Test(expected = APIException.class)
	public void setLimit_shouldNotAcceptAValueLessThanOne() throws Exception {
		new RequestContext().setLimit(0);
	}

	/**
	 * @see RequestContext#setLimit(Integer)
	 * @verifies not accept a null value
	 */
	@Test(expected = APIException.class)
	public void setLimit_shouldNotAcceptANullValue() throws Exception {
		new RequestContext().setLimit(null);
	}

	@Test(expected = APIException.class)
	public void setLimit_shouldNotAcceptAValueGreaterThanAbsoluteLimit() throws Exception {
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(RestConstants.MAX_RESULTS_ABSOLUTE_GLOBAL_PROPERTY_NAME, "1"));

		new RequestContext().setLimit(2);
	}

	/**
	 * @see RequestContext#getParameter(String)
	 * @verifies return null if request is null
	 */
	@Test
	public void getParameter_shouldReturnNullIfRequestIsNull() throws Exception {

		RequestContext requestContext = new RequestContext();

		assertNull(requestContext.getParameter("UNKOWN"));
	}

	/**
	 * @see RequestContext#getParameter(String)
	 * @verifies return null if the wanted request parameter is not present in the request
	 */
	@Test
	public void getParameter_shouldReturnNullIfTheWantedRequestParameterIsNotPresentInTheRequest() throws Exception {

		RequestContext requestContext = new RequestContext();
		MockHttpServletRequest request = new MockHttpServletRequest();
		requestContext.setRequest(request);

		assertNull(requestContext.getParameter("UNKOWN"));
	}

	/**
	 * @see RequestContext#getParameter(String)
	 * @verifies return the request parameter of given name if present in the request
	 */
	@Test
	public void getParameter_shouldReturnTheRequestParameterOfGivenNameIfPresentInTheRequest() throws Exception {

		RequestContext requestContext = new RequestContext();
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("includeAll", "true");
		requestContext.setRequest(request);

		assertThat(requestContext.getParameter("includeAll"), is("true"));
	}

	@Test
	public void getNextLink_shouldIncludeEncodedQueryAndNextStartIndex() throws Exception {
		RequestContext requestContext = new RequestContext();
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/ws/rest/v1/location");
		request.addParameter("q", "blood pressure");
		request.addParameter(RestConstants.REQUEST_PROPERTY_FOR_START_INDEX, "40");
		requestContext.setRequest(request);
		requestContext.setStartIndex(40);
		requestContext.setLimit(20);

		String nextUri = requestContext.getNextLink().getUri();

		assertTrue(nextUri.contains("q=blood+pressure"));
		assertTrue(nextUri.contains(RestConstants.REQUEST_PROPERTY_FOR_START_INDEX + "=60"));
		assertTrue(!nextUri.contains(RestConstants.REQUEST_PROPERTY_FOR_START_INDEX + "=40"));
	}

	@Test
	public void getPreviousLink_shouldRemoveExistingStartIndexAndIncludePreviousStartIndex() throws Exception {
		RequestContext requestContext = new RequestContext();
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/ws/rest/v1/location");
		request.addParameter("q", "xan");
		request.addParameter(RestConstants.REQUEST_PROPERTY_FOR_START_INDEX, "40");
		requestContext.setRequest(request);
		requestContext.setStartIndex(40);
		requestContext.setLimit(20);

		String previousUri = requestContext.getPreviousLink().getUri();

		assertTrue(previousUri.contains("q=xan"));
		assertTrue(previousUri.contains(RestConstants.REQUEST_PROPERTY_FOR_START_INDEX + "=20"));
		assertTrue(!previousUri.contains(RestConstants.REQUEST_PROPERTY_FOR_START_INDEX + "=40"));
	}

	@Test
	public void getPreviousLink_shouldNotIncludeStartIndexWhenPreviousPageStartsAtZero() throws Exception {
		RequestContext requestContext = new RequestContext();
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/ws/rest/v1/location");
		request.addParameter("q", "xan");
		request.addParameter(RestConstants.REQUEST_PROPERTY_FOR_START_INDEX, "10");
		requestContext.setRequest(request);
		requestContext.setStartIndex(10);
		requestContext.setLimit(20);

		String previousUri = requestContext.getPreviousLink().getUri();

		assertTrue(previousUri.contains("q=xan"));
		assertTrue(!previousUri.contains(RestConstants.REQUEST_PROPERTY_FOR_START_INDEX));
	}
}
