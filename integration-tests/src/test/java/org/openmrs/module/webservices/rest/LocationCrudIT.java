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

import org.junit.After;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class LocationCrudIT extends ITBase {

	private String createdLocationUuid;

	@After
	public void purgeCreatedLocation() {
		if (createdLocationUuid != null) {
			given().queryParam("purge", "true").delete("location/" + createdLocationUuid);
		}
	}

	@Test
	public void shouldCreateReadUpdateSearchRetireAndPurgeLocation() throws Exception {
		String locationName = "REST E2E Location " + System.currentTimeMillis();
		Map<String, Object> location = new HashMap<String, Object>();
		location.put("name", locationName);
		location.put("description", "Created by integration test");

		createdLocationUuid = given().contentType("application/json").body(location).post("location").then().statusCode(201)
		        .body("uuid", notNullValue()).body("name", equalTo(locationName)).extract().path("uuid");

		given().get("location/" + createdLocationUuid).then().statusCode(200).body("uuid", equalTo(createdLocationUuid))
		        .body("name", equalTo(locationName));

		Map<String, Object> update = new HashMap<String, Object>();
		update.put("name", locationName + " Updated");
		given().contentType("application/json").body(update).post("location/" + createdLocationUuid).then()
		        .statusCode(anyOf(is(200), is(204)));

		given().get("location/" + createdLocationUuid).then().statusCode(200)
		        .body("name", equalTo(locationName + " Updated"));

		given().queryParam("q", locationName + " Updated").get("location").then().statusCode(200)
		        .body("results.size()", greaterThan(0));

		given().queryParam("reason", "integration test cleanup").delete("location/" + createdLocationUuid).then()
		        .statusCode(anyOf(is(200), is(204)));

		given().queryParam("purge", "true").delete("location/" + createdLocationUuid).then()
		        .statusCode(anyOf(is(200), is(204)));
		createdLocationUuid = null;
	}
}
