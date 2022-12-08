/*
 * Copyright 2005-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.test.client;

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

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.soap.MessageFactory;

import java.net.URI;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import org.xmlunit.assertj.XmlAssert;

public class MockWebServiceServerTest {

	private WebServiceTemplate template;

	private MockWebServiceServer server;

	@BeforeEach
	public void setUp() {

		template = new WebServiceTemplate();
		template.setDefaultUri("http://example.com");

		server = MockWebServiceServer.createServer(template);
	}

	@Test
	public void createServerWebServiceTemplate() {

		WebServiceTemplate template = new WebServiceTemplate();

		MockWebServiceServer server = MockWebServiceServer.createServer(template);

		assertThat(server).isNotNull();
	}

	@Test
	public void createServerGatewaySupport() {

		MyClient client = new MyClient();

		MockWebServiceServer server = MockWebServiceServer.createServer(client);

		assertThat(server).isNotNull();
	}

	@Test
	public void createServerApplicationContextWebServiceTemplate() {

		StaticApplicationContext applicationContext = new StaticApplicationContext();
		applicationContext.registerSingleton("webServiceTemplate", WebServiceTemplate.class);
		applicationContext.refresh();

		MockWebServiceServer server = MockWebServiceServer.createServer(applicationContext);

		assertThat(server).isNotNull();
	}

	@Test
	public void createServerApplicationContextWebServiceGatewaySupport() {

		StaticApplicationContext applicationContext = new StaticApplicationContext();
		applicationContext.registerSingleton("myClient", MyClient.class);
		applicationContext.refresh();

		MockWebServiceServer server = MockWebServiceServer.createServer(applicationContext);
		assertThat(server).isNotNull();
	}

	@Test
	public void createServerApplicationContextEmpty() {

		assertThatIllegalArgumentException().isThrownBy(() -> {

			StaticApplicationContext applicationContext = new StaticApplicationContext();
			applicationContext.refresh();

			MockWebServiceServer server = MockWebServiceServer.createServer(applicationContext);
			assertThat(server).isNotNull();
		});
	}

	@Test
	public void mocks() throws Exception {

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

		server.expect(requestMatcher1).andExpect(requestMatcher2).andRespond(responseCreator);
		template.sendSourceAndReceiveToResult(uri.toString(), new StringSource("<request xmlns='http://example.com'/>"),
				new StringResult());

		verify(requestMatcher1, requestMatcher2, responseCreator);
	}

	@Test
	public void payloadMatch() {

		Source request = new StringSource("<request xmlns='http://example.com'/>");
		Source response = new StringSource("<response xmlns='http://example.com'/>");

		server.expect(payload(request)).andRespond(withPayload(response));

		StringResult result = new StringResult();
		template.sendSourceAndReceiveToResult(request, result);

		XmlAssert.assertThat(response.toString()).and(result.toString()).ignoreWhitespace().areSimilar();
	}

	@Test
	public void payloadNonMatch() {

		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {

			Source expected = new StringSource("<request xmlns='http://example.com'/>");

			server.expect(payload(expected));

			StringResult result = new StringResult();
			String actual = "<request xmlns='http://other.com'/>";
			template.sendSourceAndReceiveToResult(new StringSource(actual), result);
		});
	}

	@Test
	public void soapHeaderMatch() {

		final QName soapHeaderName = new QName("http://example.com", "mySoapHeader");

		server.expect(soapHeader(soapHeaderName));

		template.sendSourceAndReceiveToResult(new StringSource("<request xmlns='http://example.com'/>"), message -> {

			SoapMessage soapMessage = (SoapMessage) message;
			soapMessage.getSoapHeader().addHeaderElement(soapHeaderName);
		}, new StringResult());
	}

	@Test
	public void soapHeaderNonMatch() {

		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {

			QName soapHeaderName = new QName("http://example.com", "mySoapHeader");

			server.expect(soapHeader(soapHeaderName));

			template.sendSourceAndReceiveToResult(new StringSource("<request xmlns='http://example.com'/>"),
					new StringResult());
		});
	}

	@Test
	public void connectionMatch() {

		String uri = "http://example.com";
		server.expect(connectionTo(uri));

		template.sendSourceAndReceiveToResult(uri, new StringSource("<request xmlns='http://example.com'/>"),
				new StringResult());
	}

	@Test
	public void connectionNonMatch() {

		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {

			String expected = "http://expected.com";
			server.expect(connectionTo(expected));

			String actual = "http://actual.com";
			template.sendSourceAndReceiveToResult(actual, new StringSource("<request xmlns='http://example.com'/>"),
					new StringResult());
		});
	}

	@Test
	public void unexpectedConnection() {

		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {

			Source request = new StringSource("<request xmlns='http://example.com'/>");
			Source response = new StringSource("<response xmlns='http://example.com'/>");

			server.expect(payload(request)).andRespond(withPayload(response));

			template.sendSourceAndReceiveToResult(request, new StringResult());
			template.sendSourceAndReceiveToResult(request, new StringResult());
		});
	}

	@Test
	public void xsdMatch() throws Exception {

		Resource schema = new ByteArrayResource(
				"<schema xmlns=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://example.com\" elementFormDefault=\"qualified\"><element name=\"request\"/></schema>"
						.getBytes());

		server.expect(validPayload(schema));

		StringResult result = new StringResult();
		String actual = "<request xmlns='http://example.com'/>";
		template.sendSourceAndReceiveToResult(new StringSource(actual), result);
	}

	@Test
	public void xsdNonMatch() {

		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {

			Resource schema = new ByteArrayResource(
					"<schema xmlns=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://example.com\" elementFormDefault=\"qualified\"><element name=\"request\"/></schema>"
							.getBytes());

			server.expect(validPayload(schema));

			StringResult result = new StringResult();
			String actual = "<request2 xmlns='http://example.com'/>";
			template.sendSourceAndReceiveToResult(new StringSource(actual), result);
		});
	}

	@Test
	public void xpathExistsMatch() {

		final Map<String, String> ns = Collections.singletonMap("ns", "http://example.com");

		server.expect(xpath("/ns:request", ns).exists());

		template.sendSourceAndReceiveToResult(new StringSource("<request xmlns='http://example.com'/>"),
				new StringResult());
	}

	@Test
	public void xpathExistsNonMatch() {

		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {

			final Map<String, String> ns = Collections.singletonMap("ns", "http://example.com");

			server.expect(xpath("/ns:foo", ns).exists());

			template.sendSourceAndReceiveToResult(new StringSource("<request xmlns='http://example.com'/>"),
					new StringResult());
		});
	}

	@Test
	public void anythingMatch() {

		Source request = new StringSource("<request xmlns='http://example.com'/>");
		Source response = new StringSource("<response xmlns='http://example.com'/>");

		server.expect(anything()).andRespond(withPayload(response));

		StringResult result = new StringResult();
		template.sendSourceAndReceiveToResult(request, result);

		XmlAssert.assertThat(response.toString()).and(result.toString()).ignoreWhitespace().areSimilar();

		server.verify();
	}

	@Test
	public void recordWhenReplay() {

		assertThatIllegalStateException().isThrownBy(() -> {

			Source request = new StringSource("<request xmlns='http://example.com'/>");
			Source response = new StringSource("<response xmlns='http://example.com'/>");

			server.expect(anything()).andRespond(withPayload(response));
			server.expect(anything()).andRespond(withPayload(response));

			StringResult result = new StringResult();
			template.sendSourceAndReceiveToResult(request, result);

			XmlAssert.assertThat(response.toString()).and(result.toString()).ignoreWhitespace().areSimilar();

			server.expect(anything()).andRespond(withPayload(response));
		});
	}

	@Test
	public void soapEnvelopeMatch() {
		Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
		marshaller.setClassesToBeBound(EnvelopeMatcherRequest.class, EnvelopeMatcherResponse.class);

		template = new WebServiceTemplate(marshaller);
		template.setDefaultUri("https://example.com");
		server = MockWebServiceServer.createServer(template);

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

		server.expect(soapEnvelope(expectedSoapRequest)).andRespond(withSoapEnvelope(soapResponse));

		EnvelopeMatcherRequest request = new EnvelopeMatcherRequest();
		request.setMyData("123456");
		assertThat(request.getMyData()).isEqualTo("123456");
		EnvelopeMatcherResponse response = (EnvelopeMatcherResponse) template.marshalSendAndReceive(request);

		assertThat(response.getMyData()).isEqualTo("654321");
	}

	@Test
	public void verifyFailure() {

		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {

			server.expect(anything());
			server.verify();
		});
	}

	@Test
	public void verifyOnly() {
		server.verify();
	}

	@Test
	public void fault() {

		assertThatExceptionOfType(SoapFaultClientException.class).isThrownBy(() -> {

			Source request = new StringSource("<request xmlns='http://example.com'/>");

			server.expect(anything()).andRespond(withClientOrSenderFault("reason", Locale.ENGLISH));

			StringResult result = new StringResult();
			template.sendSourceAndReceiveToResult(request, result);
		});
	}

	static class MyClient extends WebServiceGatewaySupport {

	}

	@XmlRootElement(name = "EnvelopeMatcherRequest")
	private static class EnvelopeMatcherRequest {

		private String myData;

		public String getMyData() {
			return myData;
		}

		public void setMyData(String myData) {
			this.myData = myData;
		}
	}

	@XmlRootElement(name = "EnvelopeMatcherResponse")
	private static class EnvelopeMatcherResponse {

		private String myData;

		public String getMyData() {
			return myData;
		}

		public void setMyData(String myData) {
			this.myData = myData;
		}
	}
}
