/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.test;

import javax.xml.transform.Source;

import org.springframework.ws.server.endpoint.PayloadEndpoint;

public class EchoPayloadEndpoint implements PayloadEndpoint {

	@Override
	public Source invoke(Source request) {
		return request;
	}

}
