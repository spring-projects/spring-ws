package org.springframework.ws.soap.security;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;

public class SkipValidationWsSecurityInterceptorTest {

	private MessageFactory messageFactory;
	private AbstractWsSecurityInterceptor interceptor;
	private SaajSoapMessageFactory soapMessageFactory;

	@Before
	public void setUp() throws Exception {
		messageFactory = MessageFactory.newInstance();
		soapMessageFactory = new SaajSoapMessageFactory(messageFactory);
		interceptor = new AbstractWsSecurityInterceptor() {

			@Override
			protected void validateMessage(SoapMessage soapMessage, MessageContext messageContext)
					throws WsSecurityValidationException {
				fail("validation must be skipped.");
			}

			@Override
			protected void secureMessage(SoapMessage soapMessage, MessageContext messageContext)
					throws WsSecuritySecurementException {}

			@Override
			protected void cleanUp() {}
		};
		interceptor.setSkipValidationIfNoHeaderPresent(true);
	}

	@Test
	public void testSkipValidationOnNoHeader() throws Exception {
		doTestSkipValidation("noHeader-soap.xml");
	}

	@Test
	public void testSkipValidationOnEmptyHeader() throws Exception {
		doTestSkipValidation("emptyHeader-soap.xml");
	}

	@Test
	public void testSkipValidationOnNoSecurityHeader() throws Exception {
		doTestSkipValidation("noSecurityHeader-soap.xml");
	}

	private void doTestSkipValidation(String fileName) throws Exception {
		SoapMessage message = loadSaajMessage(fileName);
		MessageContext messageContext = new DefaultMessageContext(message, soapMessageFactory);
		assertTrue("handeRequest result must be true", interceptor.handleRequest(messageContext, null));

	}

	private SaajSoapMessage loadSaajMessage(String fileName) throws SOAPException, IOException {
		MimeHeaders mimeHeaders = new MimeHeaders();
		mimeHeaders.addHeader("Content-Type", "text/xml");
		Resource resource = new ClassPathResource(fileName, getClass());
		InputStream is = resource.getInputStream();
		try {
			assertTrue("Could not load SAAJ message [" + resource + "]", resource.exists());
			is = resource.getInputStream();
			return new SaajSoapMessage(messageFactory.createMessage(mimeHeaders, is));
		} finally {
			is.close();
		}
	}
}
