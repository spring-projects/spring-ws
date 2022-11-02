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

import javax.xml.transform.Source;

import org.springframework.xml.transform.StringSource;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.DefaultHandler;

public class SaxPayloadEndpointTest extends AbstractPayloadEndpointTestCase {

	@Override
	protected PayloadEndpoint createNoResponseEndpoint() {

		return new AbstractSaxPayloadEndpoint() {

			@Override
			protected Source getResponse(ContentHandler contentHandler) {
				return null;
			}

			@Override
			protected ContentHandler createContentHandler() {
				return new DefaultHandler();
			}
		};
	}

	@Override
	protected PayloadEndpoint createResponseEndpoint() {

		return new AbstractSaxPayloadEndpoint() {

			@Override
			protected ContentHandler createContentHandler() {
				return new TestContentHandler();
			}

			@Override
			protected Source getResponse(ContentHandler contentHandler) {
				return new StringSource(RESPONSE);
			}
		};
	}

	@Override
	protected PayloadEndpoint createNoRequestEndpoint() {

		return new AbstractSaxPayloadEndpoint() {

			@Override
			protected ContentHandler createContentHandler() {

				fail("Not expected");
				return null;
			}

			@Override
			protected Source getResponse(ContentHandler contentHandler) {
				return null;
			}
		};
	}

	private static class TestContentHandler extends DefaultHandler {

		@Override
		public void endElement(String uri, String localName, String qName) {

			assertThat(localName).isEqualTo(REQUEST_ELEMENT);
			assertThat(qName).isEqualTo(localName);
			assertThat(uri).isEqualTo(NAMESPACE_URI);
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) {

			assertThat(localName).isEqualTo(REQUEST_ELEMENT);
			assertThat(qName).isEqualTo(localName);
			assertThat(uri).isEqualTo(NAMESPACE_URI);
		}
	}
}
