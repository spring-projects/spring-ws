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

import java.io.IOException;

import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import org.springframework.ws.soap.SoapMessage;
import org.springframework.xml.transform.StringSource;
import org.springframework.xml.transform.TransformerHelper;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

class SoapEnvelopeDiffMatcherTests {

	@Test
	void match() throws Exception {

		String xml = "<?xml version='1.0'?>" + "<soap:Envelope xmlns:soap='http://www.w3.org/2003/05/soap-envelope'>"
				+ "<soap:Header><header xmlns='http://example.com'/></soap:Header>"
				+ "<soap:Body><payload xmlns='http://example.com'/></soap:Body>" + "</soap:Envelope>";
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
	void nonMatch() {

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

	@Test
	void matchWithXmlIgnore() throws TransformerException, IOException {
		String xml = """
				<?xml version='1.0'?>
				<soap:Envelope xmlns:soap='http://www.w3.org/2003/05/soap-envelope'>
				<soap:Header><header xmlns='http://example.com'/></soap:Header>
				<soap:Body><payload xmlns='http://example.com'>%s</payload></soap:Body>
				</soap:Envelope>""";

		String actual = String.format(xml, "1");
		DOMResult result = new DOMResult();
		TransformerHelper transformerHelper = new TransformerHelper();
		transformerHelper.transform(new StringSource(actual), result);
		SoapMessage message = createMock(SoapMessage.class);
		expect(message.getDocument()).andReturn((Document) result.getNode()).once();
		replay(message);

		String expected = String.format(xml, "${xmlunit.ignore}");
		SoapEnvelopeDiffMatcher matcher = new SoapEnvelopeDiffMatcher(new StringSource(expected));
		matcher.match(message);
	}

}
