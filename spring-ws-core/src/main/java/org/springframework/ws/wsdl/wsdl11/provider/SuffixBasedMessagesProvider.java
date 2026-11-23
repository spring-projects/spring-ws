/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.wsdl.wsdl11.provider;

import org.w3c.dom.Element;

import org.springframework.util.Assert;

/**
 * Implementation of the {@link MessagesProvider} interface that is based on suffixes.
 *
 * @author Arjen Poutsma
 * @since 1.5.1
 */
public class SuffixBasedMessagesProvider extends DefaultMessagesProvider {

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
	protected boolean isMessageElement(Element element) {
		if (super.isMessageElement(element)) {
			String elementName = getElementName(element);
			Assert.hasText(elementName, "Element has no name");
			return elementName.endsWith(getRequestSuffix()) || elementName.endsWith(getResponseSuffix())
					|| elementName.endsWith(getFaultSuffix());
		}
		else {
			return false;
		}
	}

}
