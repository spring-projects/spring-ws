/*
 * Copyright 2005-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.server.endpoint;

import static org.assertj.core.api.Assertions.*;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;
import org.springframework.xml.transform.TransformerFactoryUtils;
import org.xmlunit.assertj.XmlAssert;

public abstract class AbstractPayloadEndpointTestCase extends AbstractEndpointTestCase {

	private PayloadEndpoint endpoint;

	private Transformer transformer;

	@BeforeEach
	public void createEndpoint() throws Exception {

		endpoint = createResponseEndpoint();
		transformer = TransformerFactoryUtils.newInstance().newTransformer();
	}

	@Test
	public void testNoResponse() throws Exception {

		endpoint = createNoResponseEndpoint();
		StringSource requestSource = new StringSource(REQUEST);
		Source resultSource = endpoint.invoke(requestSource);

		assertThat(resultSource).isNull();
	}

	@Test
	public void testNoRequest() throws Exception {

		endpoint = createNoRequestEndpoint();
		Source resultSource = endpoint.invoke(null);

		assertThat(resultSource).isNull();
	}

	@Override
	protected final void testSource(Source requestSource) throws Exception {

		Source responseSource = endpoint.invoke(requestSource);

		assertThat(responseSource).isNotNull();

		StringResult result = new StringResult();
		transformer.transform(responseSource, result);

		XmlAssert.assertThat(result.toString()).and(RESPONSE).ignoreWhitespace().areSimilar();
	}

	protected abstract PayloadEndpoint createNoResponseEndpoint() throws Exception;

	protected abstract PayloadEndpoint createResponseEndpoint() throws Exception;

	protected abstract PayloadEndpoint createNoRequestEndpoint() throws Exception;
}
