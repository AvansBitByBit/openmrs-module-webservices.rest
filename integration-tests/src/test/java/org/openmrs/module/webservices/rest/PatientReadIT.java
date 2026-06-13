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

import org.junit.Assume;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.notNullValue;

public class PatientReadIT extends ITBase {

	@Test
	public void shouldSearchAndReadPatientDefaultAndFullRepresentations() throws Exception {
		List<Map<String, Object>> results = given().queryParam("q", "Horatio").get("patient").then().statusCode(200)
		        .extract().path("results");
		Assume.assumeFalse("The live test server must include a patient matching Horatio for this read-only check.",
		    results.isEmpty());

		String patientUuid = results.get(0).get("uuid").toString();

		given().get("patient/" + patientUuid).then().statusCode(200).body("uuid", equalTo(patientUuid))
		        .body("identifiers", not(empty())).body("person", notNullValue()).body("auditInfo", nullValue());

		given().queryParam("v", "full").get("patient/" + patientUuid).then().statusCode(200)
		        .body("uuid", equalTo(patientUuid)).body("auditInfo", notNullValue());
	}

	@Test
	public void shouldReturnNoPatientsForUnmatchedQuery() throws Exception {
		given().queryParam("q", "no-patient-should-match-this-query-" + System.currentTimeMillis()).get("patient").then()
		        .statusCode(200).body("results", empty());
	}
}
