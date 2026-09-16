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

package org.springframework.ws.soap.server.endpoint.adapter.method;

import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.core.MethodParameter;
import org.springframework.oxm.Unmarshaller;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.adapter.method.AbstractMethodArgumentResolverTests;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.server.endpoint.annotation.SoapHeader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link SoapHeaderElementMethodArgumentResolver}.
 *
 * @author Tareq Abedrabbo
 * @author Stephane Nicoll
 */
class SoapHeaderElementMethodArgumentResolverTests extends AbstractMethodArgumentResolverTests {

	private static final QName HEADER_QNAME = new QName(NAMESPACE_URI, "header");

	private static final String HEADER_CONTENT = "content";

	private SoapHeaderElementMethodArgumentResolver resolver;

	private MessageContext messageContext;

	private MethodParameter soapHeaderWithEmptyValue;

	private MethodParameter soapHeaderElementParameter;

	private MethodParameter soapHeaderElementListParameter;

	private MethodParameter soapHeaderMismatch;

	private MethodParameter soapHeaderMismatchList;

	private MethodParameter soapHeaderUnmarshalled;

	private MethodParameter soapHeaderUnmarshalledMismatch;

	private MethodParameter soapHeaderUnmarshalledList;

	private Unmarshaller unmarshaller;

	@BeforeEach
	void setUp() throws Exception {

		this.resolver = new SoapHeaderElementMethodArgumentResolver();
		this.unmarshaller = mock(Unmarshaller.class);
		this.messageContext = createSaajMessageContext();
		SoapMessage message = (SoapMessage) this.messageContext.getRequest();
		for (int i = 0; i < 3; i++) {
			SoapHeaderElement element = message.getSoapHeader().addHeaderElement(HEADER_QNAME);
			element.setText(HEADER_CONTENT + i);
		}
		this.soapHeaderWithEmptyValue = new MethodParameter(
				getClass().getMethod("soapHeaderWithEmptyValue", SoapHeaderElement.class), 0);
		this.soapHeaderElementParameter = new MethodParameter(
				getClass().getMethod("soapHeaderElement", SoapHeaderElement.class), 0);
		this.soapHeaderElementListParameter = new MethodParameter(
				getClass().getMethod("soapHeaderElementList", List.class), 0);
		this.soapHeaderMismatch = new MethodParameter(
				getClass().getMethod("soapHeaderMismatch", SoapHeaderElement.class), 0);
		this.soapHeaderMismatchList = new MethodParameter(getClass().getMethod("soapHeaderMismatchList", List.class),
				0);
		this.soapHeaderUnmarshalled = new MethodParameter(
				getClass().getMethod("soapHeaderUnmarshalled", MyHeader.class), 0);
		this.soapHeaderUnmarshalledMismatch = new MethodParameter(
				getClass().getMethod("soapHeaderUnmarshalledMismatch", MyHeader.class), 0);
		this.soapHeaderUnmarshalledList = new MethodParameter(
				getClass().getMethod("soapHeaderUnmarshalledList", List.class), 0);
	}

	@Test
	void supportsParameter() {

		assertThat(this.resolver.supportsParameter(this.soapHeaderElementParameter)).isTrue();
		assertThat(this.resolver.supportsParameter(this.soapHeaderElementListParameter)).isTrue();
	}

	@Test
	void failOnEmptyValue() {

		assertThatIllegalArgumentException()
			.isThrownBy(() -> this.resolver.resolveArgument(this.messageContext, this.soapHeaderWithEmptyValue));
	}

	@Test
	void resolveSoapHeaderElement() throws Exception {

		Object result = this.resolver.resolveArgument(this.messageContext, this.soapHeaderElementParameter);

		assertThat(SoapHeaderElement.class).isAssignableFrom(result.getClass());

		SoapHeaderElement element = (SoapHeaderElement) result;

		assertThat(element.getName()).isEqualTo(HEADER_QNAME);
		assertThat(element.getText()).isEqualTo(HEADER_CONTENT + "0");
	}

	@Test
	@SuppressWarnings("unchecked")
	void resolveSoapHeaderElementList() throws Exception {

		Object result = this.resolver.resolveArgument(this.messageContext, this.soapHeaderElementListParameter);

		assertThat(List.class).isAssignableFrom(result.getClass());

		List<SoapHeaderElement> elements = (List<SoapHeaderElement>) result;

		assertThat(elements.size()).isGreaterThan(1);

		for (int i = 0; i < elements.size(); i++) {

			SoapHeaderElement element = elements.get(i);

			assertThat(element.getName()).isEqualTo(HEADER_QNAME);
			assertThat(element.getText()).isEqualTo(HEADER_CONTENT + i);
		}
	}

	@Test
	void resolveSoapHeaderMismatch() throws Exception {

		Object result = this.resolver.resolveArgument(this.messageContext, this.soapHeaderMismatch);

		assertThat(result).isNull();
	}

	@Test
	void resolveSoapHeaderMismatchList() throws Exception {

		Object result = this.resolver.resolveArgument(this.messageContext, this.soapHeaderMismatchList);

		assertThat(List.class).isAssignableFrom(result.getClass());
		assertThat((List<?>) result).isEmpty();
	}

	@Test
	void supportsParameterUnmarshallerSupported() {
		when(this.unmarshaller.supports(MyHeader.class)).thenReturn(true);
		this.resolver.setUnmarshaller(this.unmarshaller);
		assertThat(this.resolver.supportsParameter(this.soapHeaderUnmarshalled)).isTrue();
		verify(this.unmarshaller).supports(MyHeader.class);
	}

	@Test
	void supportsParameterUnmarshallerUnsupported() {
		when(this.unmarshaller.supports(MyHeader.class)).thenReturn(false);
		this.resolver.setUnmarshaller(this.unmarshaller);
		assertThat(this.resolver.supportsParameter(this.soapHeaderUnmarshalled)).isFalse();
		verify(this.unmarshaller).supports(MyHeader.class);
	}

	@Test
	void supportsParameterNoUnmarshaller() {
		assertThat(this.resolver.supportsParameter(this.soapHeaderUnmarshalled)).isFalse();
	}

	@Test
	void resolveSoapHeaderUnmarshallerMissing() {
		assertThatIllegalStateException()
			.isThrownBy(() -> this.resolver.resolveArgument(this.messageContext, this.soapHeaderUnmarshalled))
			.withMessage("'unmarshaller' must be set to extract SOAP header with custom type");
	}

	@Test
	void resolveSoapHeaderUnmarshalled() throws Exception {
		MyHeader expected = new MyHeader();
		when(this.unmarshaller.unmarshal(any(Source.class))).thenReturn(expected);
		this.resolver.setUnmarshaller(this.unmarshaller);

		Object result = this.resolver.resolveArgument(this.messageContext, this.soapHeaderUnmarshalled);
		assertThat(result).isEqualTo(expected);
		verify(this.unmarshaller).unmarshal(any(Source.class));
	}

	@Test
	void resolveSoapHeaderUnmarshalledMismatch() throws Exception {
		this.resolver.setUnmarshaller(this.unmarshaller);
		Object result = this.resolver.resolveArgument(this.messageContext, this.soapHeaderUnmarshalledMismatch);

		assertThat(result).isNull();
		verifyNoInteractions(this.unmarshaller);
	}

	@Test
	void resolveSoapHeaderListUnmarshallerMissing() {
		assertThatIllegalStateException()
			.isThrownBy(() -> this.resolver.resolveArgument(this.messageContext, this.soapHeaderUnmarshalledList))
			.withMessage("'unmarshaller' must be set to extract SOAP header with custom type");
	}

	@Test
	@SuppressWarnings("unchecked")
	void resolveSoapHeaderUnmarshalledList() throws Exception {
		MyHeader first = new MyHeader();
		MyHeader second = new MyHeader();
		MyHeader third = new MyHeader();
		when(this.unmarshaller.unmarshal(any(Source.class))).thenReturn(first, second, third);
		this.resolver.setUnmarshaller(this.unmarshaller);

		Object result = this.resolver.resolveArgument(this.messageContext, this.soapHeaderUnmarshalledList);
		assertThat((List<MyHeader>) result).containsExactly(first, second, third);
		verify(this.unmarshaller, times(3)).unmarshal(any(Source.class));
	}

	public void soapHeaderWithEmptyValue(@SoapHeader("") SoapHeaderElement element) {
	}

	public void soapHeaderElement(@SoapHeader("{http://springframework.org/ws}header") SoapHeaderElement element) {
	}

	public void soapHeaderElementList(
			@SoapHeader("{http://springframework.org/ws}header") List<SoapHeaderElement> elements) {
	}

	public void soapHeaderMismatch(@SoapHeader("{http://springframework.org/ws}xxx") SoapHeaderElement element) {
	}

	public void soapHeaderMismatchList(
			@SoapHeader("{http://springframework.org/ws}xxx") List<SoapHeaderElement> elements) {
	}

	public void soapHeaderUnmarshalled(@SoapHeader("{http://springframework.org/ws}header") MyHeader header) {
	}

	public void soapHeaderUnmarshalledMismatch(@SoapHeader("{http://springframework.org/ws}xxx") MyHeader header) {
	}

	public void soapHeaderUnmarshalledList(
			@SoapHeader("{http://springframework.org/ws}header") List<MyHeader> headers) {
	}

	public static class MyHeader {

	}

}
