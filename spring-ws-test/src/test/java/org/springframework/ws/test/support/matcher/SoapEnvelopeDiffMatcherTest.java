/*
 * Copyright 2005-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.test.support.matcher;

import static org.assertj.core.api.Assertions.*;
import static org.easymock.EasyMock.*;

import javax.xml.transform.dom.DOMResult;

import org.junit.jupiter.api.Test;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.xml.transform.StringSource;
import org.springframework.xml.transform.TransformerHelper;
import org.w3c.dom.Document;

public class SoapEnvelopeDiffMatcherTest {

	@Test
	public void match() throws Exception {

		StringBuilder xmlBuilder = new StringBuilder();
		xmlBuilder.append("<?xml version='1.0'?>");
		xmlBuilder.append("<soap:Envelope xmlns:soap='http://www.w3.org/2003/05/soap-envelope'>");
		xmlBuilder.append("<soap:Header><header xmlns='http://example.com'/></soap:Header>");
		xmlBuilder.append("<soap:Body><payload xmlns='http://example.com'/></soap:Body>");
		xmlBuilder.append("</soap:Envelope>");
		String xml = xmlBuilder.toString();
		DOMResult result = new DOMResult();
		TransformerHelper transformerHelper = new TransformerHelper();
		transformerHelper.transform(new StringSource(xml), result);
		SoapMessage message = createMock(SoapMessage.class);
		expect(message.getDocument()).andReturn((Document) result.getNode()).once();
		replay(message);

		SoapEnvelopeDiffMatcher matcher = new SoapEnvelopeDiffMatcher(new StringSource(xml));
		matcher.match(message);

		verify(message);
	}

	@Test
	public void nonMatch() {

		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {

			StringBuilder xmlBuilder = new StringBuilder();
			xmlBuilder.append("<?xml version='1.0'?>");
			xmlBuilder.append("<soap:Envelope xmlns:soap='http://www.w3.org/2003/05/soap-envelope'>");
			xmlBuilder.append("<soap:Header><header xmlns='http://example.com'/></soap:Header>");
			xmlBuilder.append("<soap:Body><payload%s xmlns='http://example.com'/></soap:Body>");
			xmlBuilder.append("</soap:Envelope>");
			String xml = xmlBuilder.toString();
			String actual = String.format(xml, "1");
			DOMResult result = new DOMResult();
			TransformerHelper transformerHelper = new TransformerHelper();
			transformerHelper.transform(new StringSource(actual), result);
			SoapMessage message = createMock(SoapMessage.class);
			expect(message.getDocument()).andReturn((Document) result.getNode()).once();
			replay(message);

			String expected = String.format(xml, "2");
			SoapEnvelopeDiffMatcher matcher = new SoapEnvelopeDiffMatcher(new StringSource(expected));
			matcher.match(message);
		});
	}
}
