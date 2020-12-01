/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.soap.server.endpoint.adapter.method;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import javax.xml.namespace.QName;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.adapter.method.AbstractMethodArgumentResolverTestCase;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.server.endpoint.annotation.SoapHeader;

/**
 * @author Tareq Abedrabbo
 */
public class SoapHeaderElementMethodArgumentResolverTest extends AbstractMethodArgumentResolverTestCase {

	private static final QName HEADER_QNAME = new QName(NAMESPACE_URI, "header");

	private static final String HEADER_CONTENT = "content";

	private SoapHeaderElementMethodArgumentResolver resolver;

	private MessageContext messageContext;

	private MethodParameter soapHeaderWithEmptyValue;

	private MethodParameter soapHeaderElementParameter;

	private MethodParameter soapHeaderElementListParameter;

	private MethodParameter soapHeaderMismatch;

	private MethodParameter soapHeaderMismatchList;

	@BeforeEach
	public void setUp() throws Exception {

		resolver = new SoapHeaderElementMethodArgumentResolver();
		messageContext = createSaajMessageContext();
		SoapMessage message = (SoapMessage) messageContext.getRequest();
		for (int i = 0; i < 3; i++) {
			SoapHeaderElement element = message.getSoapHeader().addHeaderElement(HEADER_QNAME);
			element.setText(HEADER_CONTENT + i);
		}
		soapHeaderWithEmptyValue = new MethodParameter(
				getClass().getMethod("soapHeaderWithEmptyValue", SoapHeaderElement.class), 0);
		soapHeaderElementParameter = new MethodParameter(getClass().getMethod("soapHeaderElement", SoapHeaderElement.class),
				0);
		soapHeaderElementListParameter = new MethodParameter(getClass().getMethod("soapHeaderElementList", List.class), 0);
		soapHeaderMismatch = new MethodParameter(getClass().getMethod("soapHeaderMismatch", SoapHeaderElement.class), 0);
		soapHeaderMismatchList = new MethodParameter(getClass().getMethod("soapHeaderMismatchList", List.class), 0);
	}

	@Test
	public void supportsParameter() {

		assertThat(resolver.supportsParameter(soapHeaderElementParameter)).isTrue();
		assertThat(resolver.supportsParameter(soapHeaderElementListParameter)).isTrue();
	}

	@Test
	public void failOnEmptyValue() {

		assertThatIllegalArgumentException()
				.isThrownBy(() -> resolver.resolveArgument(messageContext, soapHeaderWithEmptyValue));
	}

	@Test
	public void resolveSoapHeaderElement() throws Exception {

		Object result = resolver.resolveArgument(messageContext, soapHeaderElementParameter);

		assertThat(SoapHeaderElement.class).isAssignableFrom(result.getClass());

		SoapHeaderElement element = (SoapHeaderElement) result;

		assertThat(element.getName()).isEqualTo(HEADER_QNAME);
		assertThat(element.getText()).isEqualTo(HEADER_CONTENT + "0");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void resolveSoapHeaderElementList() throws Exception {

		Object result = resolver.resolveArgument(messageContext, soapHeaderElementListParameter);

		assertThat(List.class).isAssignableFrom(result.getClass());

		List<SoapHeaderElement> elements = (List) result;

		assertThat(elements.size()).isGreaterThan(1);

		for (int i = 0; i < elements.size(); i++) {

			SoapHeaderElement element = elements.get(i);

			assertThat(element.getName()).isEqualTo(HEADER_QNAME);
			assertThat(element.getText()).isEqualTo(HEADER_CONTENT + i);
		}
	}

	@Test
	public void resolveSoapHeaderMismatch() throws Exception {

		Object result = resolver.resolveArgument(messageContext, soapHeaderMismatch);

		assertThat(result).isNull();
	}

	@Test
	public void resolveSoapHeaderMismatchList() throws Exception {

		Object result = resolver.resolveArgument(messageContext, soapHeaderMismatchList);

		assertThat(List.class).isAssignableFrom(result.getClass());
		assertThat((List<?>) result).isEmpty();
	}

	public void soapHeaderWithEmptyValue(@SoapHeader("") SoapHeaderElement element) {}

	public void soapHeaderElement(@SoapHeader("{http://springframework.org/ws}header") SoapHeaderElement element) {}

	public void soapHeaderElementList(
			@SoapHeader("{http://springframework.org/ws}header") List<SoapHeaderElement> elements) {}

	public void soapHeaderMismatch(@SoapHeader("{http://springframework.org/ws}xxx") SoapHeaderElement element) {}

	public void soapHeaderMismatchList(
			@SoapHeader("{http://springframework.org/ws}xxx") List<SoapHeaderElement> elements) {}

}
