/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.helper;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;
import org.openmrs.util.MemoryAppender;

public class ServerLogActionWrapperTest {

	private final ServerLogActionWrapper wrapper = new ServerLogActionWrapper() {

		@Override
		public MemoryAppender getMemoryAppender() {
			return null;
		}
	};

	@Test
	public void logLinePatternMatcher_shouldParseExpectedLogFormat() {
		String[] result = wrapper
		        .logLinePatternMatcher("INFO - Simple.appender(115) |2018-03-03 15:44:54,834| Info Message");

		assertArrayEquals(new String[] { "INFO", "Simple.appender(115)", "2018-03-03 15:44:54,834", "Info Message" },
		    result);
	}

	@Test(timeout = 1000)
	public void logLinePatternMatcher_shouldHandleLargeMalformedInputInLinearTime() {
		StringBuilder input = new StringBuilder("INFO - source ");
		for (int i = 0; i < 100000; i++) {
			input.append('a');
		}

		assertArrayEquals(new String[4], wrapper.logLinePatternMatcher(input.toString()));
	}
}
