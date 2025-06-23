/*
 * Copyright 2005-present the original author or authors.
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

package org.springframework.ws.test.support.matcher.xmlunit;

import jakarta.xml.soap.MessageFactory;
import org.junit.jupiter.api.Test;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.xml.transform.StringSource;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

class PayloadDiffMatcherTests {

	@Test
	void match() {

		var xml = "<element xmlns='http://example.com'/>";
		WebServiceMessage message = createMock(WebServiceMessage.class);

		expect(message.getPayloadSource()).andReturn(new StringSource(xml)).times(2);
		replay(message);

		var matcher = new PayloadDiffMatcher(new StringSource(xml));
		matcher.match(message);

		verify(message);
	}

	@Test
	void matchWithXmlIgnore() {

		var xml = "<element xmlns='http://example.com'>%s</element>";
		WebServiceMessage message = createMock(WebServiceMessage.class);

		expect(message.getPayloadSource()).andReturn(new StringSource(xml.formatted("1234"))).times(2);
		replay(message);

		var matcher = new PayloadDiffMatcher(new StringSource(xml.formatted("${xmlunit.ignore}")));
		matcher.match(message);

		verify(message);
	}

	@Test
	void matchIgnoringWhitespace() {

		var xml = "<response><success>true</success></response>";
		var xmlWithAdditionalWhitespace = "<response> <success>true</success> </response>";
		WebServiceMessage message = createMock(WebServiceMessage.class);

		expect(message.getPayloadSource()).andReturn(new StringSource(xml)).times(2);
		replay(message);

		var matcher = new PayloadDiffMatcher(new StringSource(xmlWithAdditionalWhitespace));
		matcher.match(message);

		verify(message);
	}

	@Test
	void nonMatch() {

		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {

			var actual = "<element1 xmlns='http://example.com'/>";
			WebServiceMessage message = createMock(WebServiceMessage.class);

			expect(message.getPayloadSource()).andReturn(new StringSource(actual)).times(2);
			replay(message);

			var expected = "<element2 xmlns='http://example.com'/>";
			var matcher = new PayloadDiffMatcher(new StringSource(expected));
			matcher.match(message);
		});
	}

	@Test
	void noPayload() {

		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {

			var matcher = new PayloadDiffMatcher(new StringSource("<message/>"));
			var messageFactory = MessageFactory.newInstance();
			var soapMessage = new SaajSoapMessage(messageFactory.createMessage());

			matcher.createDiff(soapMessage);
		});
	}

}
