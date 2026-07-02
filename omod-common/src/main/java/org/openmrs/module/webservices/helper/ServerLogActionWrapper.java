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

import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.util.MemoryAppender;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ServerLogActionWrapper used to serve the Server logs
 */
public abstract class ServerLogActionWrapper {
	
	public List<String[]> serverLog;
	
	public void setServerLog(List<String[]> serverLog) {
		this.serverLog = serverLog;
	}
	
	public List<String[]> getServerLog() {
		return serverLog;
	}
	
	/**
	 * Get server logs
	 * 
	 * @return List of last hundred server logs
	 */
	public List<String[]> getServerLogs() {
		// Check the GET_SERVER_LOGS privilege to serve the server logs
		Context.requirePrivilege(RestConstants.PRIV_GET_SERVER_LOGS);
		// Use the Memory Appender to retrieve the logs
		MemoryAppender memoryAppender = getMemoryAppender();

		if (memoryAppender == null) {
			return Collections.emptyList();
		}

		List<String> logLines = memoryAppender.getLogLines();
		List<String[]> finalOutput = new ArrayList<String[]>();
		for (String logLine : logLines) {
			String[] logElements = logLinePatternMatcher(logLine);
			finalOutput.add(logElements);
		}
		return finalOutput;
	}
	
	/**
	 * Match and find the patterns for log line
	 * 
	 * @param logLine Log lines from the terminal
	 * @return Array of matched patterns
	 */
	public String[] logLinePatternMatcher(String logLine) {
		String[] logElements = new String[4];
		if (logLine == null) {
			return logElements;
		}

		int sourceSeparator = logLine.indexOf(" - ");
		int timestampStart = sourceSeparator < 0 ? -1 : logLine.indexOf('|', sourceSeparator + 3);
		int messageStart = timestampStart < 0 ? -1 : logLine.indexOf('|', timestampStart + 1);
		if (sourceSeparator < 0 || timestampStart < 0 || messageStart < 0) {
			return logElements;
		}

		String level = logLine.substring(0, sourceSeparator).trim();
		if (!"INFO".equals(level) && !"ERROR".equals(level) && !"WARN".equals(level) && !"DEBUG".equals(level)) {
			return logElements;
		}

		logElements[0] = level;
		logElements[1] = logLine.substring(sourceSeparator + 3, timestampStart).trim();
		logElements[2] = logLine.substring(timestampStart + 1, messageStart).trim();
		logElements[3] = logLine.substring(messageStart + 1).trim();
		return logElements;
	}

	public abstract MemoryAppender getMemoryAppender();
}
