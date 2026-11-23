/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.support;

import javax.xml.transform.Source;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class EchoPayloadEndpoint {

	public static final String NAMESPACE = "http://springframework.org";

	public static final String LOCAL_PART = "root";

	@PayloadRoot(localPart = LOCAL_PART, namespace = NAMESPACE)
	@ResponsePayload
	public Source invoke(@RequestPayload Source request) throws Exception {
		return request;
	}

}
