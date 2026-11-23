/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.client.support.destination;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import static org.assertj.core.api.Assertions.assertThat;

class Wsdl11DestinationProviderTests {

	private Wsdl11DestinationProvider provider;

	@BeforeEach
	void setUp() {
		this.provider = new Wsdl11DestinationProvider();
	}

	@Test
	void testSimple() throws URISyntaxException {

		Resource wsdl = new ClassPathResource("simple.wsdl", getClass());
		this.provider.setWsdl(wsdl);

		URI result = this.provider.getDestination();

		assertThat(result).isEqualTo(new URI("http://example.com/myService"));
	}

	@Test
	void testComplex() throws URISyntaxException {
		Resource wsdl = new ClassPathResource("complex.wsdl", getClass());
		this.provider.setWsdl(wsdl);

		URI result = this.provider.getDestination();

		assertThat(result).isEqualTo(new URI("http://example.com/soap11"));
	}

	@Test
	void testCustomExpression() throws URISyntaxException {
		this.provider.setLocationExpression("/wsdl:definitions/wsdl:service/wsdl:port/soap12:address/@location");
		Resource wsdl = new ClassPathResource("complex.wsdl", getClass());
		this.provider.setWsdl(wsdl);

		URI result = this.provider.getDestination();

		assertThat(result).isEqualTo(new URI("http://example.com/soap12"));
	}

}
