/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.test.client.integration;

import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.test.integration.CustomerCountRequest;
import org.springframework.ws.test.integration.CustomerCountResponse;

public class CustomerClient extends WebServiceGatewaySupport {

	public int getCustomerCount() {
		CustomerCountRequest request = new CustomerCountRequest();
		request.setCustomerName("John Doe");

		CustomerCountResponse response = (CustomerCountResponse) getWebServiceTemplate().marshalSendAndReceive(request);
		return response.getCustomerCount();
	}

}
