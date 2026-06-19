/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.audit;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileAuditLogWriter implements AuditLogWriter {
	
	public static final String AUDIT_LOG_PATH_PROPERTY = "openmrs.webservices.rest.audit.log.path";
	
	private final Path auditLogPath;
	
	public FileAuditLogWriter() {
		this(resolveDefaultPath());
	}
	
	public FileAuditLogWriter(Path auditLogPath) {
		this.auditLogPath = auditLogPath;
	}
	
	@Override
	public void write(String line) throws IOException {
		Path parent = auditLogPath.getParent();
		if (parent != null) {
			Files.createDirectories(parent);
		}
		Files.write(auditLogPath, (line + System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
		    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
	}
	
	public Path getAuditLogPath() {
		return auditLogPath;
	}
	
	private static Path resolveDefaultPath() {
		String configuredPath = System.getProperty(AUDIT_LOG_PATH_PROPERTY);
		if (configuredPath != null && configuredPath.trim().length() > 0) {
			return Paths.get(configuredPath);
		}
		return Paths.get(System.getProperty("user.home"), "openmrs-webservices-rest-audit.log");
	}
}
