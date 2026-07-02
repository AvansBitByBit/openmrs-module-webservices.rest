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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang.StringUtils;
import org.openmrs.api.APIException;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.api.ResourceRegistry;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.api.SearchHandlerRegistry;
import org.openmrs.module.webservices.rest.web.representation.CustomRepresentation;
import org.openmrs.module.webservices.rest.web.representation.NamedRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.Resource;
import org.openmrs.module.webservices.rest.web.resource.api.SearchHandler;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler;

/**
 * Default implementation of the {@link RestService}.
 */
public class RestServiceImpl implements RestService {

	private final ResourceRegistry resourceRegistry;

	private final SearchHandlerRegistry searchHandlerRegistry;

	private final ExecutorService executorService;

	/**
	 * Creates the REST facade with replaceable registry implementations.
	 *
	 * @param resourceRegistry resource discovery and lookup abstraction
	 * @param searchHandlerRegistry search-handler indexing and selection abstraction
	 * @param executorService executor used for asynchronous refresh
	 */
	public RestServiceImpl(ResourceRegistry resourceRegistry, SearchHandlerRegistry searchHandlerRegistry,
	    ExecutorService executorService) {
		if (resourceRegistry == null || searchHandlerRegistry == null || executorService == null) {
			throw new IllegalArgumentException("Registry and executor dependencies are required");
		}
		this.resourceRegistry = resourceRegistry;
		this.searchHandlerRegistry = searchHandlerRegistry;
		this.executorService = executorService;
	}

	/**
	 * @see org.openmrs.module.webservices.rest.web.api.RestService#getRepresentation(java.lang.String)
	 * <strong>Should</strong> return default representation if given null
	 * <strong>Should</strong> return default representation if given string is empty
	 * <strong>Should</strong> return reference representation if given string matches the ref representation
	 *         constant
	 * <strong>Should</strong> return default representation if given string matches the default representation
	 *         constant
	 * <strong>Should</strong> return full representation if given string matches the full representation constant
	 * <strong>Should</strong> return an instance of custom representation if given string starts with the custom
	 *         representation prefix
	 * <strong>Should</strong> return an instance of named representation for given string if it is not empty and
	 *         does not match any other case
	 */
	@Override
	public Representation getRepresentation(String requested) {
		if (StringUtils.isEmpty(requested)) {
			return Representation.DEFAULT;
		}

		if (RestConstants.REPRESENTATION_REF.equals(requested)) {
			return Representation.REF;
		} else if (RestConstants.REPRESENTATION_DEFAULT.equals(requested)) {
			return Representation.DEFAULT;
		} else if (RestConstants.REPRESENTATION_FULL.equals(requested)) {
			return Representation.FULL;
		} else if (requested.startsWith(RestConstants.REPRESENTATION_CUSTOM_PREFIX)) {
			return new CustomRepresentation(requested.replace(RestConstants.REPRESENTATION_CUSTOM_PREFIX, ""));
		}

		return new NamedRepresentation(requested);
	}

	/**
	 * @see org.openmrs.module.webservices.rest.web.api.RestService#getResourceByName(String)
	 */
	@Override
	public Resource getResourceByName(String name) throws APIException {
		return resourceRegistry.getResourceByName(name);
	}

	/**
	 * @see org.openmrs.module.webservices.rest.web.api.RestService#getResourceBySupportedClass(Class)
	 */
	@Override
	public Resource getResourceBySupportedClass(Class<?> resourceClass) throws APIException {
		return resourceRegistry.getResourceBySupportedClass(resourceClass);
	}

	/**
	 * @see org.openmrs.module.webservices.rest.web.api.RestService#getSearchHandler(java.lang.String,
	 *      java.util.Map)
	 */
	@Override
	public SearchHandler getSearchHandler(String resourceName, Map<String, String[]> parameters) throws APIException {
		return searchHandlerRegistry.getSearchHandler(resourceName, parameters);
	}

	/**
	 * @see org.openmrs.module.webservices.rest.web.api.RestService#getResourceHandlers()
	 */
	@Override
	public List<DelegatingResourceHandler<?>> getResourceHandlers() throws APIException {
		return resourceRegistry.getResourceHandlers();
	}

	/**
	 * @see org.openmrs.module.webservices.rest.web.api.RestService#getAllSearchHandlers()
	 */
	@Override
	public List<SearchHandler> getAllSearchHandlers() {
		return searchHandlerRegistry.getAllSearchHandlers();
	}

	/**
	 * @see org.openmrs.module.webservices.rest.web.api.RestService#getSearchHandlers(java.lang.String)
	 */
	@Override
	public Set<SearchHandler> getSearchHandlers(String resourceName) {
		return searchHandlerRegistry.getSearchHandlers(resourceName);
	}

	/**
	 * @see RestService#initialize()
	 */
	@Override
	public void initialize() {
		resourceRegistry.refresh();
		searchHandlerRegistry.refresh();
	}

	@Override
	public void initializeAsync() {
		executorService.submit(new Runnable() {

			@Override
			public void run() {
				RestServiceImpl.this.initialize();
			}
		});
	}
}
