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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.openmrs.api.APIException;
import org.openmrs.module.webservices.rest.web.api.ResourceRegistry;
import org.openmrs.module.webservices.rest.web.api.SearchHandlerRegistry;
import org.openmrs.module.webservices.rest.web.resource.api.Resource;
import org.openmrs.module.webservices.rest.web.resource.api.SearchHandler;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler;

public class RestServiceImplDependencyTest {

	@Test
	public void methods_shouldDelegateToInjectedRegistryInterfaces() {
		ResourceRegistry resources = mock(ResourceRegistry.class);
		SearchHandlerRegistry searches = mock(SearchHandlerRegistry.class);
		ExecutorService executor = mock(ExecutorService.class);
		Resource resource = mock(Resource.class);
		SearchHandler searchHandler = mock(SearchHandler.class);
		Map<String, String[]> parameters = Collections.emptyMap();
		when(resources.getResourceByName("v1/test")).thenReturn(resource);
		when(searches.getSearchHandler("v1/test", parameters)).thenReturn(searchHandler);
		RestServiceImpl service = new RestServiceImpl(resources, searches, executor);

		assertThat(service.getResourceByName("v1/test"), is(resource));
		assertThat(service.getSearchHandler("v1/test", parameters), is(searchHandler));
		service.initialize();

		verify(resources).refresh();
		verify(searches).refresh();
	}

	@Test
	public void initializeAsync_shouldRefreshBothRegistriesThroughExecutor() {
		ResourceRegistry resources = mock(ResourceRegistry.class);
		SearchHandlerRegistry searches = mock(SearchHandlerRegistry.class);
		ExecutorService executor = mock(ExecutorService.class);
		ArgumentCaptor<Runnable> task = ArgumentCaptor.forClass(Runnable.class);
		RestServiceImpl service = new RestServiceImpl(resources, searches, executor);

		service.initializeAsync();
		verify(executor).submit(task.capture());
		task.getValue().run();

		verify(resources).refresh();
		verify(searches).refresh();
	}

	@Test
	public void constructor_shouldAcceptAlternativeRegistryImplementations() {
		Resource resource = mock(Resource.class);
		SearchHandler handler = mock(SearchHandler.class);
		RestServiceImpl service = new RestServiceImpl(new AlternativeResourceRegistry(resource),
		        new AlternativeSearchHandlerRegistry(handler), mock(ExecutorService.class));

		assertThat(service.getResourceByName("alternative"), is(resource));
		assertThat(service.getSearchHandler("alternative", Collections.<String, String[]> emptyMap()), is(handler));
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructor_shouldRejectMissingRequiredDependencies() {
		new RestServiceImpl(null, mock(SearchHandlerRegistry.class), mock(ExecutorService.class));
	}

	private static class AlternativeResourceRegistry implements ResourceRegistry {

		private final Resource resource;

		private AlternativeResourceRegistry(Resource resource) {
			this.resource = resource;
		}

		@Override
		public void refresh() {
		}

		@Override
		public Resource getResourceByName(String name) throws APIException {
			return resource;
		}

		@Override
		public Resource getResourceBySupportedClass(Class<?> supportedClass) throws APIException {
			return resource;
		}

		@Override
		public List<DelegatingResourceHandler<?>> getResourceHandlers() throws APIException {
			return Collections.emptyList();
		}
	}

	private static class AlternativeSearchHandlerRegistry implements SearchHandlerRegistry {

		private final SearchHandler handler;

		private AlternativeSearchHandlerRegistry(SearchHandler handler) {
			this.handler = handler;
		}

		@Override
		public void refresh() {
		}

		@Override
		public SearchHandler getSearchHandler(String resourceName, Map<String, String[]> parameters) throws APIException {
			return handler;
		}

		@Override
		public Set<SearchHandler> getSearchHandlers(String resourceName) {
			return Collections.singleton(handler);
		}

		@Override
		public List<SearchHandler> getAllSearchHandlers() {
			return Collections.singletonList(handler);
		}
	}
}
