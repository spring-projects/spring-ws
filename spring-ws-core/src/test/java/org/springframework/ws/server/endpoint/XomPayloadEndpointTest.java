/*
 * Copyright 2005-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.server.endpoint;

import static org.assertj.core.api.Assertions.*;

import nu.xom.Element;

public class XomPayloadEndpointTest extends AbstractPayloadEndpointTestCase {

	@Override
	protected PayloadEndpoint createNoResponseEndpoint() {

		return new AbstractXomPayloadEndpoint() {

			@Override
			protected Element invokeInternal(Element requestElement) {
				return null;
			}
		};
	}

	@Override
	protected PayloadEndpoint createResponseEndpoint() {

		return new AbstractXomPayloadEndpoint() {

			@Override
			protected Element invokeInternal(Element requestElement) {

				assertThat(requestElement).isNotNull();
				assertThat(requestElement.getLocalName()).isEqualTo(REQUEST_ELEMENT);
				assertThat(requestElement.getNamespaceURI()).isEqualTo(NAMESPACE_URI);

				return new Element(RESPONSE_ELEMENT, NAMESPACE_URI);
			}
		};
	}

	@Override
	protected PayloadEndpoint createNoRequestEndpoint() {

		return new AbstractXomPayloadEndpoint() {

			@Override
			protected Element invokeInternal(Element requestElement) throws Exception {

				assertThat(requestElement).isNull();
				return null;
			}
		};
	}

	@Override
	public void testStaxSourceEventReader() {
		// overriden, because XOM doesn't not support it
	}

	@Override
	public void testStaxSourceStreamReader() {
		// overriden, because XOM doesn't not support it
	}

}
