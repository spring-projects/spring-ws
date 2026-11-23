/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.addressing.server;

import org.springframework.ws.soap.addressing.version.Addressing200408;
import org.springframework.ws.soap.addressing.version.AddressingVersion;

class AddressingInterceptor200408Tests extends AbstractAddressingInterceptorTests {

	@Override
	protected AddressingVersion getVersion() {
		return new Addressing200408();
	}

	@Override
	protected String getTestPath() {
		return "200408";
	}

	@Override
	public void testNoneReplyTo() {
		// This version of the spec does not have none addresses
	}

}
