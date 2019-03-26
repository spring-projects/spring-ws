/*
 * Copyright 2005-2011 the original author or authors.
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

package org.springframework.ws.test.support.matcher;

import javax.xml.soap.MessageFactory;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.xml.transform.StringSource;

import org.junit.Test;

import static org.easymock.EasyMock.*;

public class PayloadDiffMatcherTest {

	@Test
	public void match() throws Exception {
		String xml = "<element xmlns='https://example.com'/>";
		WebServiceMessage message = createMock(WebServiceMessage.class);
		expect(message.getPayloadSource()).andReturn(new StringSource(xml)).times(2);
		replay(message);

		PayloadDiffMatcher matcher = new PayloadDiffMatcher(new StringSource(xml));
		matcher.match(message);

		verify(message);
	}

	@Test(expected = AssertionError.class)
	public void nonMatch() throws Exception {
		String actual = "<element1 xmlns='https://example.com'/>";
		WebServiceMessage message = createMock(WebServiceMessage.class);
		expect(message.getPayloadSource()).andReturn(new StringSource(actual)).times(2);
		replay(message);

		String expected = "<element2 xmlns='https://example.com'/>";
		PayloadDiffMatcher matcher = new PayloadDiffMatcher(new StringSource(expected));
		matcher.match(message);
	}

	@Test(expected = AssertionError.class)
	public void noPayload() throws Exception {
		PayloadDiffMatcher matcher = new PayloadDiffMatcher(new StringSource("<message/>"));
		MessageFactory messageFactory = MessageFactory.newInstance();
		SoapMessage soapMessage = new SaajSoapMessage(messageFactory.createMessage());

		matcher.createDiff(soapMessage);
	}

}
