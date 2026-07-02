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

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Test;
import org.openmrs.scheduler.SchedulerException;
import org.openmrs.scheduler.Task;
import org.openmrs.scheduler.TaskDefinition;

public class TaskServiceWrapperTest {

	@After
	public void clearInterruptStatus() {
		Thread.interrupted();
	}

	@Test
	public void runTask_shouldRestoreInterruptStatus() throws Exception {
		Task task = mock(Task.class);
		InterruptedException interruption = new InterruptedException("stop");
		doThrow(interruption).when(task).execute();

		try {
			new TestTaskServiceWrapper(task).runTask(new TaskDefinition());
			fail("Expected SchedulerException");
		}
		catch (SchedulerException e) {
			assertSame(interruption, e.getCause());
			assertTrue(Thread.currentThread().isInterrupted());
		}
	}

	@Test
	public void runTask_shouldWrapExecutionFailureWithoutInterruptingThread() throws Exception {
		Task task = mock(Task.class);
		ExecutionException failure = new ExecutionException(new IllegalStateException("failed"));
		doThrow(failure).when(task).execute();

		try {
			new TestTaskServiceWrapper(task).runTask(new TaskDefinition());
			fail("Expected SchedulerException");
		}
		catch (SchedulerException e) {
			assertSame(failure, e.getCause());
		}
	}

	private static class TestTaskServiceWrapper extends TaskServiceWrapper {

		private final Task task;

		private TestTaskServiceWrapper(Task task) {
			this.task = task;
		}

		@Override
		protected Task createTask(TaskDefinition taskDefinition) throws SchedulerException {
			return task;
		}
	}
}
