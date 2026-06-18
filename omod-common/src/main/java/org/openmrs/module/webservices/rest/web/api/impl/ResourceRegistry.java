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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.proxy.HibernateProxy;
import org.openmrs.api.APIException;
import org.openmrs.module.ModuleUtil;
import org.openmrs.module.webservices.rest.web.OpenmrsClassScanner;
import org.openmrs.module.webservices.rest.web.annotation.SubResource;
import org.openmrs.module.webservices.rest.web.api.RestHelperService;
import org.openmrs.module.webservices.rest.web.resource.api.Resource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubclassHandler;
import org.openmrs.module.webservices.rest.web.response.UnknownResourceException;
import org.openmrs.util.OpenmrsConstants;

class ResourceRegistry {

	private volatile Map<String, ResourceDefinition> resourceDefinitionsByNames;

	private volatile Map<Class<?>, Resource> resourcesBySupportedClasses;

	private final OpenmrsClassScanner openmrsClassScanner;

	private final RestHelperService restHelperService;

	ResourceRegistry(OpenmrsClassScanner openmrsClassScanner, RestHelperService restHelperService) {
		this.openmrsClassScanner = openmrsClassScanner;
		this.restHelperService = restHelperService;
	}

	void initialize() {
		if (resourceDefinitionsByNames != null) {
			return;
		}

		Map<String, ResourceDefinition> tempResourceDefinitionsByNames = new HashMap<String, ResourceDefinition>();
		Map<Class<?>, Resource> tempResourcesBySupportedClasses = new HashMap<Class<?>, Resource>();

		List<Class<? extends Resource>> resources;
		try {
			resources = openmrsClassScanner.getClasses(Resource.class, true);
		}
		catch (IOException e) {
			throw new APIException("Cannot access REST resources", e);
		}

		for (Class<? extends Resource> resource : resources) {
			ResourceMetadata resourceMetadata = getResourceMetadata(resource);
			if (resourceMetadata == null) {
				continue;
			}

			if (isResourceToBeAdded(resourceMetadata, tempResourceDefinitionsByNames.get(resourceMetadata.getName()))) {
				Resource newResource = newResource(resource);

				tempResourceDefinitionsByNames.put(resourceMetadata.getName(), new ResourceDefinition(newResource,
				        resourceMetadata.getOrder()));
				tempResourcesBySupportedClasses.put(resourceMetadata.getSupportedClass(), newResource);
			}
		}

		resourcesBySupportedClasses = tempResourcesBySupportedClasses;
		resourceDefinitionsByNames = tempResourceDefinitionsByNames;
	}

	Resource getResourceByName(String name) throws APIException {
		initialize();

		ResourceDefinition resourceDefinition = resourceDefinitionsByNames.get(name);
		if (resourceDefinition == null) {
			throw new UnknownResourceException("Unknown resource: " + name);
		} else {
			return resourceDefinition.resource;
		}
	}

	Resource getResourceBySupportedClass(Class<?> resourceClass) throws APIException {
		initialize();

		if (HibernateProxy.class.isAssignableFrom(resourceClass)) {
			resourceClass = resourceClass.getSuperclass();
		}

		Resource resource = resourcesBySupportedClasses.get(resourceClass);

		if (resource == null) {
			Entry<Class<?>, Resource> bestResourceEntry = null;

			for (Entry<Class<?>, Resource> resourceEntry : resourcesBySupportedClasses.entrySet()) {
				if (resourceEntry.getKey().isAssignableFrom(resourceClass) && (bestResourceEntry == null
				        || bestResourceEntry.getKey().isAssignableFrom(resourceEntry.getKey()))) {
					bestResourceEntry = resourceEntry;
				}
			}

			if (bestResourceEntry != null) {
				resource = bestResourceEntry.getValue();
			}
		}

		if (resource == null) {
			throw new APIException("Unknown resource: " + resourceClass);
		} else {
			return resource;
		}
	}

	List<DelegatingResourceHandler<?>> getResourceHandlers() throws APIException {
		initialize();

		List<DelegatingResourceHandler<?>> resourceHandlers = new ArrayList<DelegatingResourceHandler<?>>();

		for (Resource resource : resourcesBySupportedClasses.values()) {
			if (resource instanceof DelegatingResourceHandler) {
				resourceHandlers.add((DelegatingResourceHandler<?>) resource);
			}
		}

		List<DelegatingSubclassHandler> subclassHandlers = restHelperService.getRegisteredRegisteredSubclassHandlers();
		for (DelegatingSubclassHandler subclassHandler : subclassHandlers) {
			resourceHandlers.add(subclassHandler);
		}

		return resourceHandlers;
	}

	private boolean isResourceToBeAdded(ResourceMetadata resourceMetadata, ResourceDefinition existingResourceDefinition) {
		if (existingResourceDefinition == null) {
			return true;
		}
		if (existingResourceDefinition.order == resourceMetadata.getOrder()) {
			throw new IllegalStateException("Two resources with the same name (" + resourceMetadata.getName()
			        + ") must not have the same order");
		}

		return existingResourceDefinition.order >= resourceMetadata.getOrder();
	}

	private ResourceMetadata getResourceMetadata(Class<? extends Resource> resource) {
		ResourceMetadata resourceMetadata;

		org.openmrs.module.webservices.rest.web.annotation.Resource resourceAnnotation = resource
		        .getAnnotation(org.openmrs.module.webservices.rest.web.annotation.Resource.class);
		if (resourceAnnotation == null) {
			SubResource subresourceAnnotation = resource.getAnnotation(SubResource.class);
			if (subresourceAnnotation == null
			        || !isOpenmrsVersionInVersions(subresourceAnnotation.supportedOpenmrsVersions())) {
				return null;
			}
			org.openmrs.module.webservices.rest.web.annotation.Resource parentResourceAnnotation = subresourceAnnotation
			        .parent().getAnnotation(org.openmrs.module.webservices.rest.web.annotation.Resource.class);
			if (parentResourceAnnotation == null) {
				return null;
			}
			resourceMetadata = new ResourceMetadata(parentResourceAnnotation.name() + "/" + subresourceAnnotation.path(),
			        subresourceAnnotation.supportedClass(), subresourceAnnotation.order());
		} else {
			if (!isOpenmrsVersionInVersions(resourceAnnotation.supportedOpenmrsVersions())) {
				return null;
			}
			resourceMetadata = new ResourceMetadata(resourceAnnotation.name(), resourceAnnotation.supportedClass(),
			        resourceAnnotation.order());
		}
		return resourceMetadata;
	}

	private boolean isOpenmrsVersionInVersions(String[] versions) {
		if (versions.length == 0) {
			return false;
		}

		boolean result = false;
		for (String version : versions) {
			if (ModuleUtil.matchRequiredVersions(OpenmrsConstants.OPENMRS_VERSION_SHORT, version)) {
				result = true;
				break;
			}
		}
		return result;
	}

	private Resource newResource(Class<? extends Resource> resourceClass) {
		try {
			return resourceClass.newInstance();
		}
		catch (Exception ex) {
			throw new APIException("Failed to instantiate " + resourceClass, ex);
		}
	}

	private static class ResourceDefinition {

		private final Resource resource;

		private final int order;

		private ResourceDefinition(Resource resource, int order) {
			this.resource = resource;
			this.order = order;
		}
	}

	private static class ResourceMetadata {

		private final String name;

		private final Class<?> supportedClass;

		private final int order;

		private ResourceMetadata(String name, Class<?> supportedClass, int order) {
			this.name = name;
			this.supportedClass = supportedClass;
			this.order = order;
		}

		private String getName() {
			return name;
		}

		private Class<?> getSupportedClass() {
			return supportedClass;
		}

		private int getOrder() {
			return order;
		}
	}
}
