/*
 * Copyright 2005-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.server.endpoint;

import static org.assertj.core.api.Assertions.*;

import org.jdom2.Element;
import org.jdom2.Namespace;

public class JDomPayloadEndpointTest extends AbstractPayloadEndpointTestCase {

	@Override
	protected PayloadEndpoint createNoResponseEndpoint() throws Exception {
		return new AbstractJDomPayloadEndpoint() {

			@Override
			protected Element invokeInternal(Element requestElement) throws Exception {
				return null;
			}
		};
	}

	@Override
	protected PayloadEndpoint createResponseEndpoint() throws Exception {

		return new AbstractJDomPayloadEndpoint() {

			@Override
			protected Element invokeInternal(Element requestElement) throws Exception {

				assertThat(requestElement).isNotNull();
				assertThat(requestElement.getName()).isEqualTo(REQUEST_ELEMENT);
				assertThat(requestElement.getNamespaceURI()).isEqualTo(NAMESPACE_URI);

				return new Element(RESPONSE_ELEMENT, Namespace.getNamespace("tns", NAMESPACE_URI));
			}
		};
	}

	@Override
	protected PayloadEndpoint createNoRequestEndpoint() throws Exception {

		return new AbstractJDomPayloadEndpoint() {

			@Override
			protected Element invokeInternal(Element requestElement) throws Exception {

				assertThat(requestElement).isNull();
				return null;
			}
		};
	}
}
