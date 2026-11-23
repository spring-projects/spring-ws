/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.test.integration;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "http://springframework.org/spring-ws")
public class CustomerCountRequest {

	private String customerName;

	@XmlElement(namespace = "http://springframework.org/spring-ws")
	public String getCustomerName() {
		return this.customerName;
	}

	public void setCustomerName(String name) {
		this.customerName = name;
	}

}
