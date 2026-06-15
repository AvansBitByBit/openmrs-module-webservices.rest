/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest;

import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class ErrorHandlingIT extends ITBase {

	@Test
	public void shouldReturnUnauthenticatedSessionWithoutCredentials() throws Exception {
		given().auth().none().get("session").then().statusCode(200).body("authenticated", is(false))
		        .body("locale", notNullValue()).body("allowedLocales", notNullValue());
	}

	@Test
	public void shouldReturnNotFoundForUnknownResource() throws Exception {
		given().get("notarealresource").then().statusCode(404).body("error", notNullValue());
	}

	@Test
	public void shouldReturnNotFoundForUnknownLocationUuid() throws Exception {
		given().get("location/not-a-real-location-uuid").then().statusCode(404).body("error", notNullValue());
	}

	@Test
	public void shouldReturnBadRequestForMalformedJson() throws Exception {
		given().contentType("application/json").body("{").post("location").then().statusCode(400)
		        .body("error", notNullValue());
	}

	@Test
	public void shouldReturnMethodNotAllowedForUnsupportedSessionMethod() throws Exception {
		given().put("session").then().statusCode(405).body("error", notNullValue());
	}
}
