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
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityAuditLogger {
	
	public static final String UNKNOWN = "onbekend";
	
	private static final Logger AUDIT_LOG = LoggerFactory.getLogger(SecurityAuditLogger.class);
	
	private static final Logger INTERNAL_LOG = LoggerFactory.getLogger(SecurityAuditLogger.class.getName() + ".internal");
	
	private static final SecurityAuditLogger INSTANCE = new SecurityAuditLogger(new FileAuditLogWriter());
	
	private final AuditLogWriter writer;
	
	private final Clock clock;
	
	public SecurityAuditLogger(AuditLogWriter writer) {
		this(writer, Clock.systemUTC());
	}
	
	SecurityAuditLogger(AuditLogWriter writer, Clock clock) {
		this.writer = writer;
		this.clock = clock;
	}
	
	public static SecurityAuditLogger getInstance() {
		return INSTANCE;
	}
	
	public void loginSucceeded(String actor, String where, String why) {
		recordWarn(actor, "LOGIN_SUCCESS", where, why);
	}
	
	public void loginFailed(String actor, String where, String why) {
		recordError(actor, "LOGIN_FAILURE", where, why);
	}
	
	public void patientRecordViewed(String actor, String patientUuid, String where, String why) {
		recordWarn(actor, "PATIENT_RECORD_VIEW patientUuid=" + value(patientUuid), where, why);
	}
	
	public void patientRecordAccessDenied(String actor, String patientUuid, String where, String why) {
		recordError(actor, "PATIENT_RECORD_ACCESS_DENIED patientUuid=" + value(patientUuid), where, why);
	}
	
	public void roleOrRightsChanged(String actor, String target, String where, String why) {
		recordWarn(actor, "ROLE_OR_RIGHTS_CHANGE target=" + value(target), where, why);
	}
	
	public void dataExported(String actor, String target, String where, String why) {
		recordWarn(actor, "DATA_EXPORT target=" + value(target), where, why);
	}
	
	public void dataExportDenied(String actor, String target, String where, String why) {
		recordError(actor, "DATA_EXPORT_DENIED target=" + value(target), where, why);
	}
	
	public void recordWarn(String actor, String what, String where, String why) {
		record(actor, what, where, why, false);
	}
	
	public void recordError(String actor, String what, String where, String why) {
		record(actor, what, where, why, true);
	}
	
	public static String currentActor() {
		try {
			if (Context.isAuthenticated()) {
				User user = Context.getAuthenticatedUser();
				if (user != null) {
					return value(user.getUsername());
				}
			}
		}
		catch (RuntimeException ignored) {
			// Context may not be available in isolated unit tests or early web filter execution.
		}
		return UNKNOWN;
	}
	
	public static String where(HttpServletRequest request) {
		if (request == null) {
			return UNKNOWN;
		}
		StringBuilder where = new StringBuilder();
		where.append(value(request.getRemoteAddr()));
		where.append(" ");
		where.append(value(request.getMethod()));
		where.append(" ");
		where.append(value(request.getRequestURI()));
		return where.toString();
	}
	
	public static String where(RequestContext context) {
		if (context == null) {
			return UNKNOWN;
		}
		return where(context.getRequest());
	}
	
	private void record(String actor, String what, String where, String why, boolean error) {
		String line = format(actor, what, where, why);
		if (error) {
			AUDIT_LOG.error(line);
		} else {
			AUDIT_LOG.warn(line);
		}
		try {
			writer.write(line);
		}
		catch (IOException ex) {
			INTERNAL_LOG.error("Unable to persist security audit event", ex);
		}
	}
	
	private String format(String actor, String what, String where, String why) {
		return "audit_event wie=\""
		        + value(actor)
		        + "\" wat=\""
		        + value(what)
		        + "\" wanneer=\""
		        + OffsetDateTime.now(clock).withOffsetSameInstant(ZoneOffset.UTC)
		                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
		        + "\" waar=\"" + value(where) + "\" waarom=\"" + value(why) + "\"";
	}
	
	private static String value(String value) {
		if (value == null || value.trim().length() == 0) {
			return UNKNOWN;
		}
		return sanitize(value);
	}
	
	private static String sanitize(String value) {
		String sanitized = value.replace('\r', ' ').replace('\n', ' ').replace('\t', ' ').trim();
		sanitized = sanitized.replaceAll("(?i)\\b(authorization|cookie)\\b\\s*[:=]?\\s*(bearer|basic)?\\s*[^,;|\\s]+",
		    "$1=[REDACTED]");
		sanitized = sanitized.replaceAll("(?i)\\b(bearer|basic)\\s+[A-Za-z0-9._~+/=-]+", "$1 [REDACTED]");
		sanitized = sanitized
		        .replaceAll(
		            "(?i)\\b(password|wachtwoord|passphrase|sessionToken|session_token|token|bsn)\\b\\s*[:=]?\\s*[^,;|\\s]+",
		            "$1=[REDACTED]");
		sanitized = sanitized.replaceAll(
		    "(?i)\\b(diagnose|diagnosis|diagnoses|medicatie|medication|medicijn|medicijnen)\\b\\s*[:=]?\\s*[^,;|]+",
		    "$1=[REDACTED]");
		sanitized = sanitized.replaceAll("\\b\\d{9}\\b", "[REDACTED]");
		sanitized = sanitized.replace("\\", "\\\\").replace("\"", "'");
		return sanitized;
	}
}
