/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.server.endpoint;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xmlunit.assertj.XmlAssert;

import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;
import org.springframework.xml.transform.TransformerFactoryUtils;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractPayloadEndpointTests extends AbstractEndpointTests {

	private PayloadEndpoint endpoint;

	private Transformer transformer;

	@BeforeEach
	void createEndpoint() throws Exception {

		this.endpoint = createResponseEndpoint();
		this.transformer = TransformerFactoryUtils.newInstance().newTransformer();
	}

	@Test
	void testNoResponse() throws Exception {

		this.endpoint = createNoResponseEndpoint();
		StringSource requestSource = new StringSource(REQUEST);
		Source resultSource = this.endpoint.invoke(requestSource);

		assertThat(resultSource).isNull();
	}

	@Test
	void testNoRequest() throws Exception {

		this.endpoint = createNoRequestEndpoint();
		Source resultSource = this.endpoint.invoke(null);

		assertThat(resultSource).isNull();
	}

	@Override
	protected final void testSource(Source requestSource) throws Exception {

		Source responseSource = this.endpoint.invoke(requestSource);

		assertThat(responseSource).isNotNull();

		StringResult result = new StringResult();
		this.transformer.transform(responseSource, result);

		XmlAssert.assertThat(result.toString()).and(RESPONSE).ignoreWhitespace().areSimilar();
	}

	protected abstract PayloadEndpoint createNoResponseEndpoint() throws Exception;

	protected abstract PayloadEndpoint createResponseEndpoint() throws Exception;

	protected abstract PayloadEndpoint createNoRequestEndpoint() throws Exception;

}
