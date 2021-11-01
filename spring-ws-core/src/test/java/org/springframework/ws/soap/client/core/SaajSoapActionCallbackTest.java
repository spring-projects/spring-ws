package org.springframework.ws.soap.client.core;

import static org.assertj.core.api.Assertions.*;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPConstants;
import jakarta.xml.soap.SOAPException;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.soap.support.SoapUtils;
import org.springframework.ws.transport.TransportConstants;

public class SaajSoapActionCallbackTest {

	private SaajSoapMessageFactory saaj11Factory = new SaajSoapMessageFactory();

	private SaajSoapMessageFactory saaj12Factory = new SaajSoapMessageFactory();

	@BeforeEach
	public void init() throws SOAPException {

		MessageFactory messageFactory11 = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);

		saaj11Factory.setSoapVersion(SoapVersion.SOAP_11);
		saaj11Factory.setMessageFactory(messageFactory11);
		saaj11Factory.afterPropertiesSet();

		MessageFactory messageFactory12 = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
		saaj12Factory.setSoapVersion(SoapVersion.SOAP_12);
		saaj12Factory.setMessageFactory(messageFactory12);
		saaj12Factory.afterPropertiesSet();
	}

	@Test
	public void noSoapAction11ShouldProduceEmptySoapActionHeader() {

		SaajSoapMessage message = saaj11Factory.createWebServiceMessage();
		String[] soapActionHeaders = message.getSaajMessage().getMimeHeaders()
				.getHeader(TransportConstants.HEADER_SOAP_ACTION);

		assertThat(soapActionHeaders).containsExactly("\"\"");
	}

	@Test
	public void soapAction11ShouldProduceSoapActionHeader() throws IOException {

		SaajSoapMessage message = saaj11Factory.createWebServiceMessage();
		SoapActionCallback callback = new SoapActionCallback("testAction");
		callback.doWithMessage(message);
		String[] soapActionHeaders = message.getSaajMessage().getMimeHeaders()
				.getHeader(TransportConstants.HEADER_SOAP_ACTION);

		assertThat(soapActionHeaders).containsExactly("\"testAction\"");
	}

	@Test
	public void emptySoapAction11() throws IOException {

		SaajSoapMessage message = saaj11Factory.createWebServiceMessage();
		SoapActionCallback callback = new SoapActionCallback(null);
		callback.doWithMessage(message);
		String[] soapActionHeaders = message.getSaajMessage().getMimeHeaders()
				.getHeader(TransportConstants.HEADER_SOAP_ACTION);

		assertThat(soapActionHeaders).containsExactly("\"\"");
	}

	@Test
	public void noSoapAction12() {

		SaajSoapMessage message = saaj12Factory.createWebServiceMessage();
		String[] soapActionHeaders = message.getSaajMessage().getMimeHeaders()
				.getHeader(TransportConstants.HEADER_SOAP_ACTION);

		assertThat(soapActionHeaders).isNull();
	}

	@Test
	public void soapAction12ShouldProduceNoSoapActionHeader() throws IOException {

		SaajSoapMessage message = saaj12Factory.createWebServiceMessage();
		SoapActionCallback callback = new SoapActionCallback("testAction");
		callback.doWithMessage(message);
		String[] soapActionHeaders = message.getSaajMessage().getMimeHeaders()
				.getHeader(TransportConstants.HEADER_SOAP_ACTION);
		String[] contentTypes = message.getSaajMessage().getMimeHeaders().getHeader(TransportConstants.HEADER_CONTENT_TYPE);

		assertThat(soapActionHeaders).isNull();
		assertThat(SoapUtils.extractActionFromContentType(contentTypes[0])).isEqualTo("\"testAction\"");
	}

	@Test
	public void emptySoapAction12ShouldProduceNoSoapActionHeader() throws IOException {

		SaajSoapMessage message = saaj12Factory.createWebServiceMessage();
		SoapActionCallback callback = new SoapActionCallback(null);
		callback.doWithMessage(message);
		String[] soapActionHeaders = message.getSaajMessage().getMimeHeaders()
				.getHeader(TransportConstants.HEADER_SOAP_ACTION);
		String[] contentTypes = message.getSaajMessage().getMimeHeaders().getHeader(TransportConstants.HEADER_CONTENT_TYPE);

		assertThat(soapActionHeaders).isNull();
		assertThat(SoapUtils.extractActionFromContentType(contentTypes[0])).isEqualTo("\"\"");
	}
}
