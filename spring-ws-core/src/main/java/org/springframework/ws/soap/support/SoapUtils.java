/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.support;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.transport.TransportConstants;

/**
 * Contains various utility methods for handling SOAP messages.
 *
 * @author Arjen Poutsma
 * @author Greg Turnquist
 * @since 1.5.5
 */
public abstract class SoapUtils {

	private static final Pattern ACTION_PATTERN = Pattern.compile("action\\s*=\\s*([^;]+)");

	private SoapUtils() {
	}

	/** Escapes the given SOAP action to be surrounded by quotes. */
	public static String escapeAction(@Nullable String soapAction) {

		if (!StringUtils.hasLength(soapAction)) {
			soapAction = "\"\"";
		}
		if (!soapAction.startsWith("\"")) {
			soapAction = "\"" + soapAction;
		}
		if (!soapAction.endsWith("\"")) {
			soapAction = soapAction + "\"";
		}
		return soapAction;
	}

	/**
	 * Returns the value of the action parameter in the given SOAP 1.2 content type.
	 * @param contentType the SOAP 1.2 content type
	 * @return the action
	 */
	public static String extractActionFromContentType(String contentType) {

		if (contentType != null) {
			Matcher matcher = ACTION_PATTERN.matcher(contentType);
			if (matcher.find() && matcher.groupCount() == 1) {
				return matcher.group(1).trim();
			}
		}
		return TransportConstants.EMPTY_SOAP_ACTION;
	}

	/**
	 * Replaces or adds the value of the action parameter in the given SOAP 1.2 content
	 * type.
	 * @param contentType the SOAP 1.2 content type
	 * @param action the action
	 * @return the new content type
	 */
	public static String setActionInContentType(String contentType, String action) {

		Assert.hasLength(contentType, "'contentType' must not be empty");

		if (StringUtils.hasText(action)) {
			Matcher matcher = ACTION_PATTERN.matcher(contentType);
			if (matcher.find() && matcher.groupCount() == 1) {
				StringBuilder builder = new StringBuilder();
				matcher.appendReplacement(builder, "action=" + action);
				matcher.appendTail(builder);
				return builder.toString();
			}
			else {
				return contentType + "; action=" + action;
			}
		}
		else {
			return contentType;
		}
	}

}
