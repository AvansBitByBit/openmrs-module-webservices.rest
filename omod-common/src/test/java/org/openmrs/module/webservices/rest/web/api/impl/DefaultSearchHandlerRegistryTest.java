/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.api.impl;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.module.webservices.rest.web.api.RestHelperService;
import org.openmrs.module.webservices.rest.web.resource.api.SearchConfig;
import org.openmrs.module.webservices.rest.web.resource.api.SearchHandler;
import org.openmrs.module.webservices.rest.web.resource.api.SearchQuery;
import org.openmrs.module.webservices.rest.web.response.InvalidSearchException;
import org.openmrs.test.BaseContextMockTest;

public class DefaultSearchHandlerRegistryTest extends BaseContextMockTest {

	@Mock
	private RestHelperService restHelperService;

	private DefaultSearchHandlerRegistry registry;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setUpRegistry() {
		RestUtil.disableContext();
		registry = new DefaultSearchHandlerRegistry(restHelperService);
	}

	@Test
	public void getSearchHandler_shouldMatchExplicitId() {
		SearchHandler handler = handler("conceptByMapping", "2.8.*", "source", "code");
		when(restHelperService.getRegisteredSearchHandlers()).thenReturn(asList(handler));
		Map<String, String[]> parameters = parameters("s", "conceptByMapping");

		assertThat(registry.getSearchHandler("v1/concept", parameters), is(handler));
	}

	@Test
	public void getSearchHandler_shouldMatchRequiredAndOptionalParameters() {
		SearchHandler complete = handler("default", "2.8.*", "source", "code");
		SearchHandler requiredOnly = handler("conceptByMapping", "2.8.*", "source", null);
		when(restHelperService.getRegisteredSearchHandlers()).thenReturn(asList(complete, requiredOnly));
		Map<String, String[]> parameters = parameters("source", "SNOMED");
		parameters.put("code", new String[] { "123" });

		assertThat(registry.getSearchHandler("v1/concept", parameters), is(complete));
	}

	@Test
	public void getSearchHandler_shouldChooseDefaultWhenMultipleHandlersMatch() {
		SearchHandler defaultHandler = handler("default", "2.8.*", "source", "code");
		SearchHandler customHandler = handler("custom", "2.8.*", "source", "code");
		when(restHelperService.getRegisteredSearchHandlers()).thenReturn(asList(defaultHandler, customHandler));
		Map<String, String[]> parameters = parameters("source", "SNOMED");
		parameters.put("code", new String[] { "123" });

		assertThat(registry.getSearchHandler("v1/concept", parameters), is(defaultHandler));
	}

	@Test
	public void getSearchHandler_shouldRejectAmbiguousMatchesWithoutDefault() {
		SearchHandler first = handler("first", "2.8.*", "source", "code");
		SearchHandler second = handler("second", "2.8.*", "source", "code");
		when(restHelperService.getRegisteredSearchHandlers()).thenReturn(asList(first, second));
		Map<String, String[]> parameters = parameters("source", "SNOMED");
		parameters.put("code", new String[] { "123" });

		expectedException.expect(InvalidSearchException.class);
		expectedException.expectMessage("The search is ambiguous");
		registry.getSearchHandler("v1/concept", parameters);
	}

	@Test
	public void refresh_shouldRejectDuplicateIds() {
		SearchHandler first = handler("duplicate", "2.8.*", "source", null);
		SearchHandler second = handler("duplicate", "2.8.*", "source", null);
		when(restHelperService.getRegisteredSearchHandlers()).thenReturn(asList(first, second));

		expectedException.expect(IllegalStateException.class);
		expectedException.expectMessage("must not have the same ID");
		registry.refresh();
	}

	@Test
	public void refresh_shouldIgnoreUnsupportedVersions() {
		SearchHandler handler = handler("default", "1.8.*", "source", null);
		when(restHelperService.getRegisteredSearchHandlers()).thenReturn(asList(handler));

		registry.refresh();

		assertThat(registry.getSearchHandlers("v1/concept"), is(nullValue()));
		assertThat(registry.getAllSearchHandlers(), hasItem(handler));
	}

	@Test
	public void refresh_shouldReplacePreviouslyCachedHandlers() {
		SearchHandler first = handler("first", "2.8.*", "source", null);
		SearchHandler second = handler("second", "2.8.*", "patient", null);
		when(restHelperService.getRegisteredSearchHandlers()).thenReturn(asList(first), asList(second));

		registry.refresh();
		assertThat(registry.getSearchHandlers("v1/concept"), hasItem(first));
		registry.refresh();
		assertThat(registry.getSearchHandlers("v1/concept"), hasItem(second));
	}

	private SearchHandler handler(String id, String version, String requiredParameter, String optionalParameter) {
		SearchQuery.Builder query = new SearchQuery.Builder("test query").withRequiredParameters(requiredParameter);
		if (optionalParameter != null) {
			query.withOptionalParameters(optionalParameter);
		}
		SearchHandler handler = mock(SearchHandler.class);
		when(handler.getSearchConfig()).thenReturn(new SearchConfig(id, "v1/concept", version, query.build()));
		return handler;
	}

	private Map<String, String[]> parameters(String name, String value) {
		Map<String, String[]> parameters = new HashMap<String, String[]>();
		parameters.put(name, new String[] { value });
		return parameters;
	}
}
