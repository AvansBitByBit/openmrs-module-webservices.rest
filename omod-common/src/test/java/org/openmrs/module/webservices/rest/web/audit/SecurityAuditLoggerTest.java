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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

public class SecurityAuditLoggerTest {
	
	private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-06-19T08:15:30Z"), ZoneOffset.UTC);
	
	private Logger logger;
	
	private ListAppender<ILoggingEvent> listAppender;
	
	@Before
	public void setUp() {
		logger = (Logger) LoggerFactory.getLogger(SecurityAuditLogger.class);
		logger.setLevel(Level.WARN);
		listAppender = new ListAppender<ILoggingEvent>();
		listAppender.start();
		logger.addAppender(listAppender);
	}
	
	@After
	public void tearDown() {
		logger.detachAppender(listAppender);
		listAppender.stop();
	}
	
	@Test
	public void shouldLogSuccessfulSecurityActionsWithFiveWs() throws Exception {
		AuditLogWriter writer = mock(AuditLogWriter.class);
		SecurityAuditLogger auditLogger = new SecurityAuditLogger(writer, FIXED_CLOCK);
		
		auditLogger.loginSucceeded("arts.jansen", "10.0.0.5 POST /ws/rest/v1/session", "Basic authentication accepted");
		auditLogger.patientRecordViewed("arts.jansen", "patient-uuid-123", "10.0.0.5 GET /ws/rest/v1/patient/patient-uuid-123",
		    "behandeling");
		auditLogger.roleOrRightsChanged("admin", "role-uuid-456", "10.0.0.6 POST /ws/rest/v1/role/role-uuid-456",
		    "autorisatiebeheer");
		auditLogger.dataExported("admin", "obsUuid=obs-uuid-789", "10.0.0.6 GET /ws/rest/v1/obs/obs-uuid-789/value",
		    "gegevensoverdracht");
		
		assertEquals(4, listAppender.list.size());
		for (ILoggingEvent event : listAppender.list) {
			assertEquals(Level.WARN, event.getLevel());
			assertContainsFiveWs(event.getFormattedMessage());
		}
		assertTrue(listAppender.list.get(0).getFormattedMessage().contains("wat=\"LOGIN_SUCCESS\""));
		assertTrue(listAppender.list.get(1).getFormattedMessage().contains("wat=\"PATIENT_RECORD_VIEW patientUuid=patient-uuid-123\""));
		assertTrue(listAppender.list.get(2).getFormattedMessage().contains("wat=\"ROLE_OR_RIGHTS_CHANGE target=role-uuid-456\""));
		assertTrue(listAppender.list.get(3).getFormattedMessage().contains("wat=\"DATA_EXPORT target=obsUuid=obs-uuid-789\""));
		verify(writer, times(4)).write(anyString());
	}
	
	@Test
	public void shouldLogFailedSecurityActionsAtErrorLevel() throws Exception {
		AuditLogWriter writer = mock(AuditLogWriter.class);
		SecurityAuditLogger auditLogger = new SecurityAuditLogger(writer, FIXED_CLOCK);
		
		auditLogger.loginFailed("arts.jansen", "10.0.0.5 POST /ws/rest/v1/session", "Invalid credentials");
		auditLogger.loginLockout("arts.jansen", "10.0.0.5 POST /ws/rest/v1/session", "Authentication locked");
		auditLogger.loginLockoutDenied("arts.jansen", "10.0.0.5 POST /ws/rest/v1/session", "Authentication locked");
		auditLogger.insecureTransportDenied("arts.jansen", "10.0.0.5 POST /ws/rest/v1/session", "HTTPS required");
		auditLogger.patientRecordAccessDenied("arts.jansen", "patient-uuid-123",
		    "10.0.0.5 GET /ws/rest/v1/patient/patient-uuid-123", "Access denied");
		auditLogger.dataExportDenied("arts.jansen", "obsUuid=obs-uuid-789",
		    "10.0.0.5 GET /ws/rest/v1/obs/obs-uuid-789/value", "Access denied");
		
		assertEquals(6, listAppender.list.size());
		for (ILoggingEvent event : listAppender.list) {
			assertEquals(Level.ERROR, event.getLevel());
			assertContainsFiveWs(event.getFormattedMessage());
		}
		assertTrue(listAppender.list.get(0).getFormattedMessage().contains("wat=\"LOGIN_FAILURE\""));
		assertTrue(listAppender.list.get(1).getFormattedMessage().contains("wat=\"LOGIN_LOCKOUT\""));
		assertTrue(listAppender.list.get(2).getFormattedMessage().contains("wat=\"LOGIN_LOCKOUT_DENIED\""));
		assertTrue(listAppender.list.get(3).getFormattedMessage().contains("wat=\"INSECURE_TRANSPORT_DENIED\""));
		assertTrue(listAppender.list.get(4).getFormattedMessage().contains("wat=\"PATIENT_RECORD_ACCESS_DENIED"));
		assertTrue(listAppender.list.get(5).getFormattedMessage().contains("wat=\"DATA_EXPORT_DENIED"));
		verify(writer, times(6)).write(anyString());
	}
	
	@Test
	public void shouldNotWriteSensitiveDataToLogOrPersistentWriter() throws Exception {
		AuditLogWriter writer = mock(AuditLogWriter.class);
		SecurityAuditLogger auditLogger = new SecurityAuditLogger(writer, FIXED_CLOCK);
		
		auditLogger.loginFailed("arts.jansen password=SuperSecret", "10.0.0.5 Authorization=Bearer abc.def.ghi",
		    "wachtwoord hunter2 BSN=123456789 diagnose diabetes type 2; medicatie oxycodon; sessionToken=s3ss10n");
		
		ArgumentCaptor<String> persistedLine = ArgumentCaptor.forClass(String.class);
		verify(writer).write(persistedLine.capture());
		
		String logLine = listAppender.list.get(0).getFormattedMessage();
		assertEquals(logLine, persistedLine.getValue());
		assertContainsFiveWs(logLine);
		assertFalse(logLine.contains("SuperSecret"));
		assertFalse(logLine.contains("hunter2"));
		assertFalse(logLine.contains("123456789"));
		assertFalse(logLine.contains("diabetes"));
		assertFalse(logLine.contains("oxycodon"));
		assertFalse(logLine.contains("s3ss10n"));
		assertFalse(logLine.contains("abc.def.ghi"));
		assertTrue(logLine.contains("[REDACTED]"));
	}
	
	@Test
	public void shouldPersistAuditLinesToFile() throws Exception {
		Path auditFile = Files.createTempFile("webservices-rest-audit", ".log");
		FileAuditLogWriter writer = new FileAuditLogWriter(auditFile);
		SecurityAuditLogger auditLogger = new SecurityAuditLogger(writer, FIXED_CLOCK);
		
		auditLogger.roleOrRightsChanged("admin", "role-uuid-456", "10.0.0.6 PUT /ws/rest/v1/role/role-uuid-456",
		    "autorisatiebeheer");
		
		List<String> persistedLines = Files.readAllLines(auditFile, StandardCharsets.UTF_8);
		assertEquals(1, persistedLines.size());
		assertEquals(listAppender.list.get(0).getFormattedMessage(), persistedLines.get(0));
		assertContainsFiveWs(persistedLines.get(0));
	}
	
	private void assertContainsFiveWs(String logLine) {
		assertTrue(logLine.contains("wie=\""));
		assertTrue(logLine.contains("wat=\""));
		assertTrue(logLine.contains("wanneer=\"2026-06-19T08:15:30Z\""));
		assertTrue(logLine.contains("waar=\""));
		assertTrue(logLine.contains("waarom=\""));
	}
}
