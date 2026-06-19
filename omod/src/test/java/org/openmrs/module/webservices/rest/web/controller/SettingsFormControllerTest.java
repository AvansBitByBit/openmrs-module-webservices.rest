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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;

public class SettingsFormControllerTest extends BaseModuleWebContextSensitiveTest {
	
	private SettingsFormController controller;
	
	@Before
	public void before() {
		controller = Context.getRegisteredComponents(SettingsFormController.class).iterator().next();
	}
	
	@Test(expected = ContextAuthenticationException.class)
	public void searchProperties_shouldRequireManageRestwsPrivilege() {
		Context.logout();
		
		controller.searchProperties("webservices.rest");
	}
	
	@Test
	public void searchProperties_shouldReturnPropertyNamesWithoutValues() {
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty("webservices.rest.secret.password", "SuperSecretValue"));
		
		List<SimpleObject> result = controller.searchProperties("webservices.rest.secret");
		
		Assert.assertEquals(1, result.size());
		Assert.assertEquals("webservices.rest.secret.password", result.get(0).get("property"));
		Assert.assertFalse(result.get(0).containsKey("value"));
		Assert.assertFalse(result.toString().contains("SuperSecretValue"));
	}
	
	@Test(expected = ContextAuthenticationException.class)
	public void showForm_shouldRequireManageRestwsPrivilege() {
		Context.logout();
		
		controller.showForm();
	}
	
	@Test(expected = ContextAuthenticationException.class)
	public void getModel_shouldRequireManageRestwsPrivilege() {
		Context.logout();
		
		controller.getModel();
	}
}
