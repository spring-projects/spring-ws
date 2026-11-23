/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.wsdl.wsdl11.provider;

import javax.wsdl.Message;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;

/**
 * Implementation of the {@link PortTypesProvider} interface that is based on suffixes.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public class SuffixBasedPortTypesProvider extends AbstractPortTypesProvider {

	/** The default suffix used to detect request elements in the schema. */
	public static final String DEFAULT_REQUEST_SUFFIX = "Request";

	/** The default suffix used to detect response elements in the schema. */
	public static final String DEFAULT_RESPONSE_SUFFIX = "Response";

	/** The default suffix used to detect fault elements in the schema. */
	public static final String DEFAULT_FAULT_SUFFIX = "Fault";

	private String requestSuffix = DEFAULT_REQUEST_SUFFIX;

	private String responseSuffix = DEFAULT_RESPONSE_SUFFIX;

	private String faultSuffix = DEFAULT_FAULT_SUFFIX;

	/**
	 * Returns the suffix used to detect request elements in the schema.
	 * @see #DEFAULT_REQUEST_SUFFIX
	 */
	public String getRequestSuffix() {
		return this.requestSuffix;
	}

	/**
	 * Sets the suffix used to detect request elements in the schema.
	 * @see #DEFAULT_REQUEST_SUFFIX
	 */
	public void setRequestSuffix(String requestSuffix) {
		Assert.hasText(requestSuffix, "'requestSuffix' must not be empty");
		this.requestSuffix = requestSuffix;
	}

	/**
	 * Returns the suffix used to detect response elements in the schema.
	 * @see #DEFAULT_RESPONSE_SUFFIX
	 */
	public String getResponseSuffix() {
		return this.responseSuffix;
	}

	/**
	 * Sets the suffix used to detect response elements in the schema.
	 * @see #DEFAULT_RESPONSE_SUFFIX
	 */
	public void setResponseSuffix(String responseSuffix) {
		Assert.hasText(responseSuffix, "'responseSuffix' must not be empty");
		this.responseSuffix = responseSuffix;
	}

	/**
	 * Returns the suffix used to detect fault elements in the schema.
	 * @see #DEFAULT_FAULT_SUFFIX
	 */
	public String getFaultSuffix() {
		return this.faultSuffix;
	}

	/**
	 * Sets the suffix used to detect fault elements in the schema.
	 * @see #DEFAULT_FAULT_SUFFIX
	 */
	public void setFaultSuffix(String faultSuffix) {
		Assert.hasText(faultSuffix, "'faultSuffix' must not be empty");
		this.faultSuffix = faultSuffix;
	}

	@Override
	protected @Nullable String getOperationName(Message message) {
		String messageName = getMessageName(message);
		if (messageName != null) {
			if (messageName.endsWith(getRequestSuffix())) {
				return messageName.substring(0, messageName.length() - getRequestSuffix().length());
			}
			else if (messageName.endsWith(getResponseSuffix())) {
				return messageName.substring(0, messageName.length() - getResponseSuffix().length());
			}
			else if (messageName.endsWith(getFaultSuffix())) {
				return messageName.substring(0, messageName.length() - getFaultSuffix().length());
			}
		}
		return null;
	}

	/**
	 * Indicates whether the given name name should be included as
	 * {@link javax.wsdl.Input} message in the definition.
	 * <p>
	 * This implementation checks whether the message name ends with the
	 * {@link #setRequestSuffix(String) requestSuffix}.
	 * @param message the message
	 * @return {@code true} if to be included as input; {@code false} otherwise
	 */
	@Override
	protected boolean isInputMessage(Message message) {
		String messageName = getMessageName(message);
		return messageName != null && messageName.endsWith(getRequestSuffix());
	}

	/**
	 * Indicates whether the given name name should be included as
	 * {@link javax.wsdl.Output} message in the definition.
	 * <p>
	 * This implementation checks whether the message name ends with the
	 * {@link #setResponseSuffix(String) responseSuffix}.
	 * @param message the message
	 * @return {@code true} if to be included as output; {@code false} otherwise
	 */
	@Override
	protected boolean isOutputMessage(Message message) {
		String messageName = getMessageName(message);
		return messageName != null && messageName.endsWith(getResponseSuffix());
	}

	/**
	 * Indicates whether the given name name should be included as
	 * {@link javax.wsdl.Fault} message in the definition.
	 * <p>
	 * This implementation checks whether the message name ends with the
	 * {@link #setFaultSuffix(String) faultSuffix}.
	 * @param message the message
	 * @return {@code true} if to be included as fault; {@code false} otherwise
	 */
	@Override
	protected boolean isFaultMessage(Message message) {
		String messageName = getMessageName(message);
		return messageName != null && messageName.endsWith(getFaultSuffix());
	}

	private @Nullable String getMessageName(Message message) {
		return message.getQName().getLocalPart();
	}

}
