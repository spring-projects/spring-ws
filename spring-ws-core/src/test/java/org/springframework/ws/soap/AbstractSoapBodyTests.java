/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap;

import java.util.Locale;

import javax.xml.transform.dom.DOMResult;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xmlunit.assertj.XmlAssert;

import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractSoapBodyTests extends AbstractSoapElementTests {

	protected SoapBody soapBody;

	@Override
	protected final SoapElement createSoapElement() throws Exception {

		this.soapBody = createSoapBody();
		return this.soapBody;
	}

	protected abstract SoapBody createSoapBody() throws Exception;

	@Test
	void testPayload() throws Exception {

		String payload = "<payload xmlns='http://www.springframework.org' />";
		this.transformer.transform(new StringSource(payload), this.soapBody.getPayloadResult());

		assertPayloadEqual(payload);
	}

	@Test
	void testGetPayloadResultTwice() throws Exception {

		String payload = "<payload xmlns='http://www.springframework.org' />";
		this.transformer.transform(new StringSource(payload), this.soapBody.getPayloadResult());
		this.transformer.transform(new StringSource(payload), this.soapBody.getPayloadResult());
		DOMResult domResult = new DOMResult();
		this.transformer.transform(this.soapBody.getSource(), domResult);
		Element bodyElement = ((Document) domResult.getNode()).getDocumentElement();
		NodeList children = bodyElement.getChildNodes();

		assertThat(children.getLength()).isEqualTo(1);
	}

	@Test
	void testNoFault() {
		assertThat(this.soapBody.hasFault()).isFalse();
	}

	@Test
	void testAddFaultWithExistingPayload() throws Exception {

		StringSource contents = new StringSource("<payload xmlns='http://www.springframework.org' />");
		this.transformer.transform(contents, this.soapBody.getPayloadResult());
		this.soapBody.addMustUnderstandFault("faultString", Locale.ENGLISH);

		assertThat(this.soapBody.hasFault()).isTrue();
	}

	protected void assertPayloadEqual(String expectedPayload) throws Exception {

		StringResult result = new StringResult();
		this.transformer.transform(this.soapBody.getPayloadSource(), result);

		XmlAssert.assertThat(result.toString()).and(expectedPayload).ignoreWhitespace().areSimilar();
	}

}
