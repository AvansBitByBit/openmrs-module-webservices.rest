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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;

public class SwaggerDocControllerTest {

	@Test
	public void debugEndpoint_shouldNotExposeUserControlledHtml() throws Exception {
		MockMvc mockMvc = standaloneSetup(new SwaggerDocController()).build();

		mockMvc.perform(get("/module/webservices/rest/apiDocs/debug").param("tag", "<script>alert(1)</script>"))
		        .andExpect(status().isNotFound());
	}
}
