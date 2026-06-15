/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9;

import java.util.HashMap;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.PatientIdentifierResource1_8;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;

public class CreatePatientIdentifierResource1_9Test extends BaseModuleWebContextSensitiveTest {
	
	private PatientIdentifierResource1_8 resource;
	
	@Before
	public void beforeEachTests() throws Exception {
		resource = (PatientIdentifierResource1_8) Context.getService(RestService.class).getResourceBySupportedClass(
		    PatientIdentifier.class);
	}
	
	@Test
	public void shouldCreatePatientIdentifier_WhenTypeIsSpecifiedByUuid() throws Exception {
		
		String identifier = "REST-UUID-IDENTIFIER";
		String personAttributeJson = "{" + "            \"identifier\": \"" + identifier + "\","
		        + "            \"identifierType\": {" + "              \"uuid\" : \"2f470aa8-1d73-43b7-81b5-01f0c0dfa53c\""
		        + "            },"
		        + "            \"location\" : {" + "              \"uuid\" : \"8d6c993e-c2cc-11de-8d13-0010c6dffd0f\""
		        + "            }," + "            \"preferred\": false" + "        }";
		
		SimpleObject personAttributeSimpleObject = new SimpleObject();
		personAttributeSimpleObject.putAll(new ObjectMapper().readValue(personAttributeJson, HashMap.class));
		
		SimpleObject created = (SimpleObject) resource.create("da7f524f-27ce-4bb2-86d6-6d1d05312bd5",
		    personAttributeSimpleObject, new RequestContext());
		Assert.assertEquals(identifier, created.get("identifier"));
	}
	
	@Test
	public void shouldCreatePatientIdentifier_WhenTypeIsSpecifiedByName() throws Exception {
		String identifier = "REST-NAME-IDENTIFIER";
		String personAttributeJson = "{" + "            \"identifier\": \"" + identifier + "\","
		        + "            \"identifierType\": {" + "              \"name\" : \"Old Identification Number\"" + "            },"
		        + "            \"location\" : {" + "              \"uuid\" : \"8d6c993e-c2cc-11de-8d13-0010c6dffd0f\""
		        + "            }," + "            \"preferred\": false" + "        }";
		
		SimpleObject personAttributeSimpleObject = new SimpleObject();
		personAttributeSimpleObject.putAll(new ObjectMapper().readValue(personAttributeJson, HashMap.class));
		
		SimpleObject created = (SimpleObject) resource.create("da7f524f-27ce-4bb2-86d6-6d1d05312bd5",
		    personAttributeSimpleObject, new RequestContext());
		Assert.assertEquals(identifier, created.get("identifier"));
	}
	
}
