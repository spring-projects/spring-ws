/*
 * Copyright 2005-2022 the original author or authors.
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

package org.springframework.ws.test.support.matcher.xmlunit2;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import jakarta.xml.soap.MessageFactory;

import org.junit.jupiter.api.Test;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.xml.transform.StringSource;

public class PayloadDiffMatcherTest {

	@Test
	public void match() {

		String xml = "<element xmlns='http://example.com'/>";
		WebServiceMessage message = createMock(WebServiceMessage.class);
		expect(message.getPayloadSource()).andReturn(new StringSource(xml)).times(2);
		replay(message);

		PayloadDiffMatcher matcher = new PayloadDiffMatcher(new StringSource(xml));
		matcher.match(message);

		verify(message);
	}

	@Test
	public void matchIgnoringWhitespace() {

		String xml = "<response><success>true</success></response>";
		String xmlWithAdditionalWhitespace = "<response> <success>true</success> </response>";
		WebServiceMessage message = createMock(WebServiceMessage.class);
		expect(message.getPayloadSource()).andReturn(new StringSource(xml)).times(2);
		replay(message);

		PayloadDiffMatcher matcher = new PayloadDiffMatcher(new StringSource(xmlWithAdditionalWhitespace));
		matcher.match(message);

		verify(message);
	}


	@Test
	public void nonMatch() {

		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {

			String actual = "<element1 xmlns='http://example.com'/>";
			WebServiceMessage message = createMock(WebServiceMessage.class);
			expect(message.getPayloadSource()).andReturn(new StringSource(actual)).times(2);
			replay(message);

			String expected = "<element2 xmlns='http://example.com'/>";
			PayloadDiffMatcher matcher = new PayloadDiffMatcher(new StringSource(expected));
			matcher.match(message);
		});
	}

	@Test
	public void noPayload() {

		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {

			PayloadDiffMatcher matcher = new PayloadDiffMatcher(new StringSource("<message/>"));
			MessageFactory messageFactory = MessageFactory.newInstance();
			SoapMessage soapMessage = new SaajSoapMessage(messageFactory.createMessage());

			matcher.createDiff(soapMessage);
		});
	}

}
