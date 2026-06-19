/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.filter;

import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class AuthenticationRateLimiter {
	
	private final Map<String, FailureState> failures = new ConcurrentHashMap<String, FailureState>();
	
	private final Clock clock;
	
	AuthenticationRateLimiter() {
		this(Clock.systemUTC());
	}
	
	AuthenticationRateLimiter(Clock clock) {
		this.clock = clock;
	}
	
	boolean isLocked(String username, String remoteAddress, int maxFailures, long windowMillis, long lockoutMillis) {
		FailureState state = failures.get(key(username, remoteAddress));
		long now = now();
		if (state == null) {
			return false;
		}
		if (state.lockedUntilMillis <= now) {
			return false;
		}
		return true;
	}
	
	boolean recordFailure(String username, String remoteAddress, int maxFailures, long windowMillis, long lockoutMillis) {
		long now = now();
		FailureState state = failures.get(key(username, remoteAddress));
		if (state == null || now - state.firstFailureMillis > windowMillis) {
			state = new FailureState(now);
		}
		state.failureCount++;
		if (state.failureCount >= maxFailures) {
			state.lockedUntilMillis = now + lockoutMillis;
		}
		failures.put(key(username, remoteAddress), state);
		return state.lockedUntilMillis > now;
	}
	
	void recordSuccess(String username, String remoteAddress) {
		failures.remove(key(username, remoteAddress));
	}
	
	void reset() {
		failures.clear();
	}
	
	private long now() {
		return clock.millis();
	}
	
	private String key(String username, String remoteAddress) {
		return String.valueOf(username).toLowerCase() + "|" + String.valueOf(remoteAddress);
	}
	
	private static class FailureState {
		
		private final long firstFailureMillis;
		
		private int failureCount;
		
		private long lockedUntilMillis;
		
		private FailureState(long firstFailureMillis) {
			this.firstFailureMillis = firstFailureMillis;
		}
	}
}
