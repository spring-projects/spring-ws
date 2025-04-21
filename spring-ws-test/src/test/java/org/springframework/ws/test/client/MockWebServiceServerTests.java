/*
 * Copyright 2005-2025 the original author or authors.
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

package org.springframework.ws.test.client;

import java.net.URI;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.soap.MessageFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xmlunit.assertj.XmlAssert;

import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.client.SoapFaultClientException;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.springframework.ws.test.client.RequestMatchers.anything;
import static org.springframework.ws.test.client.RequestMatchers.connectionTo;
import static org.springframework.ws.test.client.RequestMatchers.payload;
import static org.springframework.ws.test.client.RequestMatchers.soapEnvelope;
import static org.springframework.ws.test.client.RequestMatchers.soapHeader;
import static org.springframework.ws.test.client.RequestMatchers.validPayload;
import static org.springframework.ws.test.client.RequestMatchers.xpath;
import static org.springframework.ws.test.client.ResponseCreators.withClientOrSenderFault;
import static org.springframework.ws.test.client.ResponseCreators.withPayload;
import static org.springframework.ws.test.client.ResponseCreators.withSoapEnvelope;

class MockWebServiceServerTests {

	private WebServiceTemplate template;

	private MockWebServiceServer server;

	@BeforeEach
	void setUp() {

		this.template = new WebServiceTemplate();
		this.template.setDefaultUri("http://example.com");

		this.server = MockWebServiceServer.createServer(this.template);
	}

	@Test
	void createServerWebServiceTemplate() {

		WebServiceTemplate template = new WebServiceTemplate();

		MockWebServiceServer server = MockWebServiceServer.createServer(template);

		assertThat(server).isNotNull();
	}

	@Test
	void createServerGatewaySupport() {

		MyClient client = new MyClient();

		MockWebServiceServer server = MockWebServiceServer.createServer(client);

		assertThat(server).isNotNull();
	}

	@Test
	void createServerApplicationContextWebServiceTemplate() {

		StaticApplicationContext applicationContext = new StaticApplicationContext();
		applicationContext.registerSingleton("webServiceTemplate", WebServiceTemplate.class);
		applicationContext.refresh();

		MockWebServiceServer server = MockWebServiceServer.createServer(applicationContext);

		assertThat(server).isNotNull();
	}

	@Test
	void createServerApplicationContextWebServiceGatewaySupport() {

		StaticApplicationContext applicationContext = new StaticApplicationContext();
		applicationContext.registerSingleton("myClient", MyClient.class);
		applicationContext.refresh();

		MockWebServiceServer server = MockWebServiceServer.createServer(applicationContext);
		assertThat(server).isNotNull();
	}

	@Test
	void createServerApplicationContextEmpty() {

		assertThatIllegalArgumentException().isThrownBy(() -> {

			StaticApplicationContext applicationContext = new StaticApplicationContext();
			applicationContext.refresh();

			MockWebServiceServer server = MockWebServiceServer.createServer(applicationContext);
			assertThat(server).isNotNull();
		});
	}

	@Test
	void mocks() throws Exception {

		URI uri = URI.create("http://example.com");

		RequestMatcher requestMatcher1 = createStrictMock("requestMatcher1", RequestMatcher.class);
		RequestMatcher requestMatcher2 = createStrictMock("requestMatcher2", RequestMatcher.class);
		ResponseCreator responseCreator = createStrictMock(ResponseCreator.class);

		SaajSoapMessage response = new SaajSoapMessageFactory(MessageFactory.newInstance()).createWebServiceMessage();

		requestMatcher1.match(eq(uri), isA(SaajSoapMessage.class));
		requestMatcher2.match(eq(uri), isA(SaajSoapMessage.class));
		expect(responseCreator.createResponse(eq(uri), isA(SaajSoapMessage.class), isA(SaajSoapMessageFactory.class)))
			.andReturn(response);

		replay(requestMatcher1, requestMatcher2, responseCreator);

		this.server.expect(requestMatcher1).andExpect(requestMatcher2).andRespond(responseCreator);
		this.template.sendSourceAndReceiveToResult(uri.toString(),
				new StringSource("<request xmlns='http://example.com'/>"), new StringResult());

		verify(requestMatcher1, requestMatcher2, responseCreator);
	}

	@Test
	void payloadMatch() {

		Source request = new StringSource("<request xmlns='http://example.com'/>");
		Source response = new StringSource("<response xmlns='http://example.com'/>");

		this.server.expect(payload(request)).andRespond(withPayload(response));

		StringResult result = new StringResult();
		this.template.sendSourceAndReceiveToResult(request, result);

		XmlAssert.assertThat(response.toString()).and(result.toString()).ignoreWhitespace().areSimilar();
	}

	@Test
	void payloadNonMatch() {

		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {

			Source expected = new StringSource("<request xmlns='http://example.com'/>");

			this.server.expect(payload(expected));

			StringResult result = new StringResult();
			String actual = "<request xmlns='http://other.com'/>";
			this.template.sendSourceAndReceiveToResult(new StringSource(actual), result);
		});
	}

	@Test
	void soapHeaderMatch() {

		final QName soapHeaderName = new QName("http://example.com", "mySoapHeader");

		this.server.expect(soapHeader(soapHeaderName));

		this.template.sendSourceAndReceiveToResult(new StringSource("<request xmlns='http://example.com'/>"),
				message -> {

					SoapMessage soapMessage = (SoapMessage) message;
					soapMessage.getSoapHeader().addHeaderElement(soapHeaderName);
				}, new StringResult());
	}

	@Test
	void soapHeaderNonMatch() {

		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {

			QName soapHeaderName = new QName("http://example.com", "mySoapHeader");

			this.server.expect(soapHeader(soapHeaderName));

			this.template.sendSourceAndReceiveToResult(new StringSource("<request xmlns='http://example.com'/>"),
					new StringResult());
		});
	}

	@Test
	void connectionMatch() {

		String uri = "http://example.com";
		this.server.expect(connectionTo(uri));

		this.template.sendSourceAndReceiveToResult(uri, new StringSource("<request xmlns='http://example.com'/>"),
				new StringResult());
	}

	@Test
	void connectionNonMatch() {

		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {

			String expected = "http://expected.com";
			this.server.expect(connectionTo(expected));

			String actual = "http://actual.com";
			this.template.sendSourceAndReceiveToResult(actual,
					new StringSource("<request xmlns='http://example.com'/>"), new StringResult());
		});
	}

	@Test
	void unexpectedConnection() {

		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {

			Source request = new StringSource("<request xmlns='http://example.com'/>");
			Source response = new StringSource("<response xmlns='http://example.com'/>");

			this.server.expect(payload(request)).andRespond(withPayload(response));

			this.template.sendSourceAndReceiveToResult(request, new StringResult());
			this.template.sendSourceAndReceiveToResult(request, new StringResult());
		});
	}

	@Test
	void xsdMatch() throws Exception {

		Resource schema = new ByteArrayResource(
				"<schema xmlns=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://example.com\" elementFormDefault=\"qualified\"><element name=\"request\"/></schema>"
					.getBytes());

		this.server.expect(validPayload(schema));

		StringResult result = new StringResult();
		String actual = "<request xmlns='http://example.com'/>";
		this.template.sendSourceAndReceiveToResult(new StringSource(actual), result);
	}

	@Test
	void xsdNonMatch() {

		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {

			Resource schema = new ByteArrayResource(
					"<schema xmlns=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://example.com\" elementFormDefault=\"qualified\"><element name=\"request\"/></schema>"
						.getBytes());

			this.server.expect(validPayload(schema));

			StringResult result = new StringResult();
			String actual = "<request2 xmlns='http://example.com'/>";
			this.template.sendSourceAndReceiveToResult(new StringSource(actual), result);
		});
	}

	@Test
	void xpathExistsMatch() {

		final Map<String, String> ns = Collections.singletonMap("ns", "http://example.com");

		this.server.expect(xpath("/ns:request", ns).exists());

		this.template.sendSourceAndReceiveToResult(new StringSource("<request xmlns='http://example.com'/>"),
				new StringResult());
	}

	@Test
	void xpathExistsNonMatch() {

		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {

			final Map<String, String> ns = Collections.singletonMap("ns", "http://example.com");

			this.server.expect(xpath("/ns:foo", ns).exists());

			this.template.sendSourceAndReceiveToResult(new StringSource("<request xmlns='http://example.com'/>"),
					new StringResult());
		});
	}

	@Test
	void anythingMatch() {

		Source request = new StringSource("<request xmlns='http://example.com'/>");
		Source response = new StringSource("<response xmlns='http://example.com'/>");

		this.server.expect(anything()).andRespond(withPayload(response));

		StringResult result = new StringResult();
		this.template.sendSourceAndReceiveToResult(request, result);

		XmlAssert.assertThat(response.toString()).and(result.toString()).ignoreWhitespace().areSimilar();

		this.server.verify();
	}

	@Test
	void recordWhenReplay() {

		assertThatIllegalStateException().isThrownBy(() -> {

			Source request = new StringSource("<request xmlns='http://example.com'/>");
			Source response = new StringSource("<response xmlns='http://example.com'/>");

			this.server.expect(anything()).andRespond(withPayload(response));
			this.server.expect(anything()).andRespond(withPayload(response));

			StringResult result = new StringResult();
			this.template.sendSourceAndReceiveToResult(request, result);

			XmlAssert.assertThat(response.toString()).and(result.toString()).ignoreWhitespace().areSimilar();

			this.server.expect(anything()).andRespond(withPayload(response));
		});
	}

	@Test
	void soapEnvelopeMatch() {
		Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
		marshaller.setClassesToBeBound(EnvelopeMatcherRequest.class, EnvelopeMatcherResponse.class);

		this.template = new WebServiceTemplate(marshaller);
		this.template.setDefaultUri("https://example.com");
		this.server = MockWebServiceServer.createServer(this.template);

		Source expectedSoapRequest = new StringSource("""
				<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
					 <SOAP-ENV:Header/>
					 <SOAP-ENV:Body>
						 <EnvelopeMatcherRequest>
							 <myData>123456</myData>
						 </EnvelopeMatcherRequest>
					 </SOAP-ENV:Body>
				</SOAP-ENV:Envelope>""");
		Source soapResponse = new StringSource("""
				<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
				  <SOAP-ENV:Body>
				    <EnvelopeMatcherResponse>
				      <myData>654321</myData>
				    </EnvelopeMatcherResponse>
				  </SOAP-ENV:Body>
				</SOAP-ENV:Envelope>""");

		this.server.expect(soapEnvelope(expectedSoapRequest)).andRespond(withSoapEnvelope(soapResponse));

		EnvelopeMatcherRequest request = new EnvelopeMatcherRequest();
		request.setMyData("123456");
		assertThat(request.getMyData()).isEqualTo("123456");
		EnvelopeMatcherResponse response = (EnvelopeMatcherResponse) this.template.marshalSendAndReceive(request);

		assertThat(response.getMyData()).isEqualTo("654321");
	}

	@Test
	void verifyFailure() {

		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {

			this.server.expect(anything());
			this.server.verify();
		});
	}

	@Test
	void verifyOnly() {
		this.server.verify();
	}

	@Test
	void fault() {

		assertThatExceptionOfType(SoapFaultClientException.class).isThrownBy(() -> {

			Source request = new StringSource("<request xmlns='http://example.com'/>");

			this.server.expect(anything()).andRespond(withClientOrSenderFault("reason", Locale.ENGLISH));

			StringResult result = new StringResult();
			this.template.sendSourceAndReceiveToResult(request, result);
		});
	}

	static class MyClient extends WebServiceGatewaySupport {

	}

	@XmlRootElement(name = "EnvelopeMatcherRequest")
	private static final class EnvelopeMatcherRequest {

		private String myData;

		public String getMyData() {
			return this.myData;
		}

		public void setMyData(String myData) {
			this.myData = myData;
		}

	}

	@XmlRootElement(name = "EnvelopeMatcherResponse")
	private static final class EnvelopeMatcherResponse {

		private String myData;

		public String getMyData() {
			return this.myData;
		}

		public void setMyData(String myData) {
			this.myData = myData;
		}

	}

}
