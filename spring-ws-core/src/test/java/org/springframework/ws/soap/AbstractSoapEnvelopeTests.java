/*
 * Copyright 2005-2025 the original author or authors.
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

package org.springframework.ws.soap;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractSoapEnvelopeTests extends AbstractSoapElementTests {

	protected SoapEnvelope soapEnvelope;

	@Override
	protected final SoapElement createSoapElement() throws Exception {

		this.soapEnvelope = createSoapEnvelope();
		return this.soapEnvelope;
	}

	protected abstract SoapEnvelope createSoapEnvelope() throws Exception;

	@Test
	void testGetHeader() {

		SoapHeader header = this.soapEnvelope.getHeader();

		assertThat(header).isNotNull();
	}

	@Test
	void testGetBody() {

		SoapBody body = this.soapEnvelope.getBody();

		assertThat(body).isNotNull();
	}

}
