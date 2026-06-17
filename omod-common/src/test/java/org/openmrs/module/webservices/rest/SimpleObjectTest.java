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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class SimpleObjectTest {

	@Test
	public void add_shouldAddPropertyAndReturnTheSameSimpleObject() {
		SimpleObject simpleObject = new SimpleObject();

		SimpleObject returned = simpleObject.add("name", "Xanadu");

		Assert.assertSame(simpleObject, returned);
		Assert.assertEquals("Xanadu", simpleObject.get("name"));
	}

	@Test
	public void removeProperty_shouldRemovePropertyAndReturnTheSameSimpleObject() {
		SimpleObject simpleObject = new SimpleObject();
		simpleObject.add("name", "Xanadu");

		SimpleObject returned = simpleObject.removeProperty("name");

		Assert.assertSame(simpleObject, returned);
		Assert.assertFalse(simpleObject.containsKey("name"));
	}

	@Test
	public void get_shouldReturnTypedPropertyValue() {
		SimpleObject simpleObject = new SimpleObject();
		simpleObject.add("count", Integer.valueOf(3));

		Integer count = simpleObject.get("count");

		Assert.assertEquals(Integer.valueOf(3), count);
	}

	@Test
	public void parseJson_shouldParseAFlatJsonObject() throws Exception {
		SimpleObject simpleObject = SimpleObject.parseJson("{\"name\":\"Xanadu\",\"retired\":false}");

		Assert.assertEquals("Xanadu", simpleObject.get("name"));
		Assert.assertEquals(Boolean.FALSE, simpleObject.get("retired"));
	}

	@Test
	public void parseJson_shouldParseNestedJsonObjectsAndArrays() throws Exception {
		SimpleObject simpleObject = SimpleObject.parseJson(
		    "{\"location\":{\"name\":\"Xanadu\"},\"tags\":[{\"name\":\"Admission\"}]}");

		Map<String, Object> location = simpleObject.get("location");
		List<Map<String, Object>> tags = simpleObject.get("tags");

		Assert.assertEquals("Xanadu", location.get("name"));
		Assert.assertEquals("Admission", tags.get(0).get("name"));
	}

	@Test(expected = IOException.class)
	public void parseJson_shouldFailForInvalidJson() throws Exception {
		SimpleObject.parseJson("{\"name\":\"Xanadu\"");
	}
}
