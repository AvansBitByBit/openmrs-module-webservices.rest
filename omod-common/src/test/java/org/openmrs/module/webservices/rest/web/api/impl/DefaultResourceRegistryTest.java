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
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockingbird.test.Animal;
import org.mockingbird.test.Country;
import org.mockingbird.test.HibernateProxyAnimal;
import org.mockingbird.test.MockingBird;
import org.mockingbird.test.rest.resource.AnimalResource_1_11;
import org.mockingbird.test.rest.resource.AnimalResource_1_9;
import org.mockingbird.test.rest.resource.BirdResource_1_9;
import org.mockingbird.test.rest.resource.CatSubclassHandler_1_11;
import org.mockingbird.test.rest.resource.CatSubclassHandler_1_9;
import org.mockingbird.test.rest.resource.CountryResource_1_9;
import org.mockingbird.test.rest.resource.DuplicateNameAndOrderAnimalResource_1_9;
import org.mockingbird.test.rest.resource.InstantiateExceptionAnimalResource_1_9;
import org.mockito.Mock;
import org.openmrs.api.APIException;
import org.openmrs.module.webservices.rest.web.OpenmrsClassScanner;
import org.openmrs.module.webservices.rest.web.api.RestHelperService;
import org.openmrs.module.webservices.rest.web.resource.api.Resource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubclassHandler;
import org.openmrs.module.webservices.rest.web.response.UnknownResourceException;
import org.openmrs.test.BaseContextMockTest;

public class DefaultResourceRegistryTest extends BaseContextMockTest {

	@Mock
	private OpenmrsClassScanner openmrsClassScanner;

	@Mock
	private RestHelperService restHelperService;

	private DefaultResourceRegistry registry;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setUpRegistry() {
		registry = new DefaultResourceRegistry(openmrsClassScanner, restHelperService);
	}

	@Test
	public void lookups_shouldResolveResourcesByNameAndSupportedClass() throws Exception {
		when(openmrsClassScanner.getClasses(Resource.class, true)).thenReturn(
		    asList(AnimalResource_1_9.class, CountryResource_1_9.class));

		assertThat(registry.getResourceByName("v1/animal"), instanceOf(AnimalResource_1_9.class));
		assertThat(registry.getResourceBySupportedClass(Country.class), instanceOf(CountryResource_1_9.class));
	}

	@Test
	public void getResourceBySupportedClass_shouldSelectNearestSuperclassAndHandleHibernateProxy() throws Exception {
		when(openmrsClassScanner.getClasses(Resource.class, true)).thenReturn(
		    asList(AnimalResource_1_9.class, AnimalResource_1_11.class, BirdResource_1_9.class));

		assertThat(registry.getResourceBySupportedClass(MockingBird.class), instanceOf(BirdResource_1_9.class));
		assertThat(registry.getResourceBySupportedClass(HibernateProxyAnimal.class),
		    instanceOf(AnimalResource_1_9.class));
	}

	@Test
	public void lookups_shouldIgnoreResourcesForUnsupportedOpenmrsVersions() throws Exception {
		when(openmrsClassScanner.getClasses(Resource.class, true)).thenReturn(asList(AnimalResource_1_11.class));

		expectedException.expect(UnknownResourceException.class);
		expectedException.expectMessage("Unknown resource: v1/animal");
		registry.getResourceByName("v1/animal");
	}

	@Test
	public void refresh_shouldReplacePreviouslyCachedResources() throws Exception {
		List<Class<? extends Resource>> initial = new ArrayList<Class<? extends Resource>>();
		initial.add(AnimalResource_1_9.class);
		List<Class<? extends Resource>> changed = new ArrayList<Class<? extends Resource>>(initial);
		changed.add(CountryResource_1_9.class);
		when(openmrsClassScanner.getClasses(Resource.class, true)).thenReturn(initial, changed);

		registry.refresh();
		assertThat(registry.getResourceBySupportedClass(Animal.class), instanceOf(AnimalResource_1_9.class));
		registry.refresh();
		assertThat(registry.getResourceBySupportedClass(Country.class), instanceOf(CountryResource_1_9.class));
	}

	@Test
	public void refresh_shouldRejectDuplicateResourceNameAndOrder() throws Exception {
		when(openmrsClassScanner.getClasses(Resource.class, true)).thenReturn(
		    asList(AnimalResource_1_9.class, DuplicateNameAndOrderAnimalResource_1_9.class));

		expectedException.expect(IllegalStateException.class);
		expectedException.expectMessage("must not have the same order");
		registry.refresh();
	}

	@Test
	public void refresh_shouldWrapScannerFailures() throws Exception {
		IOException failure = new IOException("scanner unavailable");
		when(openmrsClassScanner.getClasses(Resource.class, true)).thenThrow(failure);

		expectedException.expect(APIException.class);
		expectedException.expectCause(is(failure));
		registry.refresh();
	}

	@Test
	public void refresh_shouldWrapResourceInstantiationFailures() throws Exception {
		when(openmrsClassScanner.getClasses(Resource.class, true)).thenReturn(
		    asList(InstantiateExceptionAnimalResource_1_9.class));

		expectedException.expect(APIException.class);
		expectedException.expectMessage("Failed to instantiate");
		registry.refresh();
	}

	@Test
	public void getResourceHandlers_shouldIncludeRegisteredSubclassHandlers() throws Exception {
		CatSubclassHandler_1_9 first = mock(CatSubclassHandler_1_9.class);
		CatSubclassHandler_1_11 second = mock(CatSubclassHandler_1_11.class);
		when(openmrsClassScanner.getClasses(Resource.class, true)).thenReturn(
		    asList(AnimalResource_1_9.class, AnimalResource_1_11.class, CountryResource_1_9.class));
		when(restHelperService.getRegisteredRegisteredSubclassHandlers()).thenReturn(
		    Arrays.<DelegatingSubclassHandler> asList(first, second));

		List<DelegatingResourceHandler<?>> handlers = registry.getResourceHandlers();

		assertThat(handlers.size(), is(3));
		assertThat(handlers, hasItem(first));
		assertThat(handlers, hasItem(second));
		for (DelegatingResourceHandler<?> handler : handlers) {
			assertThat(handler, is(not(instanceOf(CountryResource_1_9.class))));
		}
	}
}
