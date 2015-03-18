/*
 * Copyright 2005-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.soap.security.xwss.callback;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.sun.xml.wss.impl.callback.TimestampValidationCallback;

/**
 * A default implementation of a {@code TimestampValidationCallback.TimestampValidator}. Based on a version found
 * in the JWSDP samples.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public class DefaultTimestampValidator implements TimestampValidationCallback.TimestampValidator {

	@Override
	public void validate(TimestampValidationCallback.Request request)
			throws TimestampValidationCallback.TimestampValidationException {
		if (request instanceof TimestampValidationCallback.UTCTimestampRequest) {
			TimestampValidationCallback.UTCTimestampRequest utcRequest =
					(TimestampValidationCallback.UTCTimestampRequest) request;
			Date created = parseDate(utcRequest.getCreated());

			validateCreationTime(created, utcRequest.getMaxClockSkew(), utcRequest.getTimestampFreshnessLimit());

			if (utcRequest.getExpired() != null) {
				Date expired = parseDate(utcRequest.getExpired());
				validateExpirationTime(expired, utcRequest.getMaxClockSkew());
			}
		}
		else {
			throw new TimestampValidationCallback.TimestampValidationException("Unsupport request: [" + request + "]");
		}
	}

	private Date getFreshnessAndSkewAdjustedDate(long maxClockSkew, long timestampFreshnessLimit) {
		Calendar c = new GregorianCalendar();
		long offset = c.get(Calendar.ZONE_OFFSET);
		if (c.getTimeZone().inDaylightTime(c.getTime())) {
			offset += c.getTimeZone().getDSTSavings();
		}
		long beforeTime = c.getTimeInMillis();
		long currentTime = beforeTime - offset;

		long adjustedTime = currentTime - maxClockSkew - timestampFreshnessLimit;
		c.setTimeInMillis(adjustedTime);

		return c.getTime();
	}

	private Date getGMTDateWithSkewAdjusted(Calendar calendar, long maxClockSkew, boolean addSkew) {
		long offset = calendar.get(Calendar.ZONE_OFFSET);
		if (calendar.getTimeZone().inDaylightTime(calendar.getTime())) {
			offset += calendar.getTimeZone().getDSTSavings();
		}
		long beforeTime = calendar.getTimeInMillis();
		long currentTime = beforeTime - offset;

		if (addSkew) {
			currentTime = currentTime + maxClockSkew;
		}
		else {
			currentTime = currentTime - maxClockSkew;
		}

		calendar.setTimeInMillis(currentTime);
		return calendar.getTime();
	}

	private Date parseDate(String date) throws TimestampValidationCallback.TimestampValidationException {
		SimpleDateFormat calendarFormatter1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		SimpleDateFormat calendarFormatter2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSS'Z'");

		try {
			try {
				return calendarFormatter1.parse(date);
			}
			catch (ParseException ignored) {
				return calendarFormatter2.parse(date);
			}
		}
		catch (ParseException ex) {
			throw new TimestampValidationCallback.TimestampValidationException("Could not parse request date: " + date,
					ex);
		}
	}

	private void validateCreationTime(Date created, long maxClockSkew, long timestampFreshnessLimit)
			throws TimestampValidationCallback.TimestampValidationException {
		Date current = getFreshnessAndSkewAdjustedDate(maxClockSkew, timestampFreshnessLimit);

		if (created.before(current)) {
			throw new TimestampValidationCallback.TimestampValidationException(
					"The creation time is older than  currenttime - timestamp-freshness-limit - max-clock-skew");
		}

		Date currentTime = getGMTDateWithSkewAdjusted(new GregorianCalendar(), maxClockSkew, true);
		if (currentTime.before(created)) {
			throw new TimestampValidationCallback.TimestampValidationException(
					"The creation time is ahead of the current time.");
		}
	}

	private void validateExpirationTime(Date expires, long maxClockSkew)
			throws TimestampValidationCallback.TimestampValidationException {
		Date currentTime = getGMTDateWithSkewAdjusted(new GregorianCalendar(), maxClockSkew, false);
		if (expires.before(currentTime)) {
			throw new TimestampValidationCallback.TimestampValidationException(
					"The current time is ahead of the expiration time in Timestamp");
		}
	}

}
