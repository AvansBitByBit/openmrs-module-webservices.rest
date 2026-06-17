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
import org.openmrs.module.webservices.rest.web.OpenmrsClassScanner;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.api.RestHelperService;
import org.openmrs.module.webservices.rest.web.api.RestService;
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

	private RestHelperService restHelperService;

	private OpenmrsClassScanner openmrsClassScanner;

	private ExecutorService executorService;

	private volatile ResourceRegistry resourceRegistry;

	private volatile SearchHandlerRegistry searchHandlerRegistry;

	public RestServiceImpl() {
	}

	public RestHelperService getRestHelperService() {
		return restHelperService;
	}

	public void setRestHelperService(RestHelperService restHelperService) {
		this.restHelperService = restHelperService;
		resourceRegistry = null;
		searchHandlerRegistry = null;
	}

	public OpenmrsClassScanner getOpenmrsClassScanner() {
		return openmrsClassScanner;
	}

	public void setOpenmrsClassScanner(OpenmrsClassScanner openmrsClassScanner) {
		this.openmrsClassScanner = openmrsClassScanner;
		resourceRegistry = null;
	}

	public ExecutorService getExecutorService() {
		return executorService;
	}

	public void setExecutorService(ExecutorService executorService) {
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
		return getResourceRegistry().getResourceByName(name);
	}

	/**
	 * @see org.openmrs.module.webservices.rest.web.api.RestService#getResourceBySupportedClass(Class)
	 */
	@Override
	public Resource getResourceBySupportedClass(Class<?> resourceClass) throws APIException {
		return getResourceRegistry().getResourceBySupportedClass(resourceClass);
	}

	/**
	 * @see org.openmrs.module.webservices.rest.web.api.RestService#getSearchHandler(java.lang.String,
	 *      java.util.Map)
	 */
	@Override
	public SearchHandler getSearchHandler(String resourceName, Map<String, String[]> parameters) throws APIException {
		return getSearchHandlerRegistry().getSearchHandler(resourceName, parameters);
	}

	/**
	 * @see org.openmrs.module.webservices.rest.web.api.RestService#getResourceHandlers()
	 */
	@Override
	public List<DelegatingResourceHandler<?>> getResourceHandlers() throws APIException {
		return getResourceRegistry().getResourceHandlers();
	}

	/**
	 * @see org.openmrs.module.webservices.rest.web.api.RestService#getAllSearchHandlers()
	 */
	public List<SearchHandler> getAllSearchHandlers() {
		return searchHandlerRegistry == null ? null : searchHandlerRegistry.getAllSearchHandlers();
	}

	/**
	 * @see org.openmrs.module.webservices.rest.web.api.RestService#getSearchHandlers(java.lang.String)
	 */
	@Override
	public Set<SearchHandler> getSearchHandlers(String resourceName) {
		return getSearchHandlerRegistry().getSearchHandlers(resourceName);
	}

	/**
	 * @see RestService#initialize()
	 */
	@Override
	public void initialize() {
		resourceRegistry = null;
		searchHandlerRegistry = null;

		getResourceRegistry().initialize();
		getSearchHandlerRegistry().initialize();
	}

	@Override
	public void initializeAsync() {
		final RestServiceImpl restService = this;
		executorService.submit(new Runnable() {

			@Override
			public void run() {
				restService.initialize();
			}
		});
	}

	private ResourceRegistry getResourceRegistry() {
		ResourceRegistry registry = resourceRegistry;
		if (registry == null) {
			registry = new ResourceRegistry(openmrsClassScanner, restHelperService);
			resourceRegistry = registry;
		}
		return registry;
	}

	private SearchHandlerRegistry getSearchHandlerRegistry() {
		SearchHandlerRegistry registry = searchHandlerRegistry;
		if (registry == null) {
			registry = new SearchHandlerRegistry(restHelperService);
			searchHandlerRegistry = registry;
		}
		return registry;
	}
}
