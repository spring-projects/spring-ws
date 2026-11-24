/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.support;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SoapUtilsTests {

	@Test
	void testExtractActionFromContentType() {

		String soapAction = "http://springframework.org/spring-ws/Action";

		String contentType = "application/soap+xml; action=" + soapAction;
		String result = SoapUtils.extractActionFromContentType(contentType);

		assertThat(result).isEqualTo(soapAction);

		contentType = "application/soap+xml; action	  = " + soapAction;
		result = SoapUtils.extractActionFromContentType(contentType);

		assertThat(result).isEqualTo(soapAction);

		contentType = "application/soap+xml; action=" + soapAction + " ; charset=UTF-8";
		result = SoapUtils.extractActionFromContentType(contentType);

		assertThat(result).isEqualTo(soapAction);

		contentType = "application/soap+xml; charset=UTF-8; action=" + soapAction;
		result = SoapUtils.extractActionFromContentType(contentType);

		assertThat(result).isEqualTo(soapAction);
	}

	@Test
	void testEscapeAction() {

		String result = SoapUtils.escapeAction("action");

		assertThat(result).isEqualTo("\"action\"");

		result = SoapUtils.escapeAction("\"action\"");

		assertThat(result).isEqualTo("\"action\"");

		result = SoapUtils.escapeAction("");

		assertThat(result).isEqualTo("\"\"");

		result = SoapUtils.escapeAction(null);

		assertThat(result).isEqualTo("\"\"");

	}

	@Test
	void unescapeActionRemovesSurroundingQuotes() {
		assertThat(SoapUtils.unescapeAction("\"action\"")).isEqualTo("action");
		assertThat(SoapUtils.unescapeAction("\"\"")).isEmpty();
	}

	@Test
	void unescapeActionWithoutSurroundingQuotesReturnsTheValueAsIs() {
		assertThat(SoapUtils.unescapeAction("action")).isEqualTo("action");
		assertThat(SoapUtils.unescapeAction("\"action")).isEqualTo("\"action");
		assertThat(SoapUtils.unescapeAction("action\"")).isEqualTo("action\"");
		assertThat(SoapUtils.unescapeAction("")).isEmpty();
		assertThat(SoapUtils.unescapeAction(null)).isNull();
	}

	@Test
	void unescapeActionWithASingleQuoteReturnsTheValueAsIs() {
		assertThat(SoapUtils.unescapeAction("\"")).isEqualTo("\"");
	}

	@Test
	void testSetActionInContentType() {

		String soapAction = "http://springframework.org/spring-ws/Action";
		String contentType = "application/soap+xml";

		String result = SoapUtils.setActionInContentType(contentType, soapAction);

		assertThat(SoapUtils.extractActionFromContentType(result)).isEqualTo(soapAction);

		String anotherSoapAction = "http://springframework.org/spring-ws/AnotherAction";
		String contentTypeWithAction = "application/soap+xml; action=http://springframework.org/spring-ws/Action";
		result = SoapUtils.setActionInContentType(contentTypeWithAction, anotherSoapAction);

		assertThat(SoapUtils.extractActionFromContentType(result)).isEqualTo(anotherSoapAction);
	}

}
