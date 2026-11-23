/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.test.server.integration;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import org.springframework.ws.test.integration.CustomerCountRequest;
import org.springframework.ws.test.integration.CustomerCountResponse;

@Endpoint
public class CustomerEndpoint {

	@ResponsePayload
	public CustomerCountResponse getCustomerCount(@RequestPayload CustomerCountRequest request) {

		CustomerCountResponse response = new CustomerCountResponse();
		response.setCustomerCount(42);
		return response;
	}

}
