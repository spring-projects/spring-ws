package org.springframework.ws.soap.security.wss4j2;

import org.apache.wss4j.dom.handler.RequestData;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.xml.transform.StringSource;
import org.springframework.xml.transform.TransformerFactoryUtils;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.util.AssertionErrors.assertEquals;

public class SaajWss4jSecurityInterceptorDefaultsTest extends Wss4jTestCase {

	private static final String PAYLOAD = "<tru:StockSymbol xmlns:tru=\"http://fabrikam123.com/payloads\">QQQ</tru:StockSymbol>";


	@Test
	public void testThatTheDefaultValueForAddInclusivePrefixesMatchesWss4JDefaultValue() {
		Wss4jSecurityInterceptor subject = new Wss4jSecurityInterceptor();
		RequestData requestData = new RequestData();
		Boolean springDefault = (Boolean) ReflectionTestUtils.getField(subject, Wss4jSecurityInterceptor.class, "addInclusivePrefixes");
		assertEquals("Spring-ws default for addInclusivePrefixes matches Wss4j default", requestData.isAddInclusivePrefixes(), springDefault);
	}

	@Test
	public void testThatInitializeValidationRequestDataSetsInclusivePrefixesUsingDefaults() throws TransformerException, SOAPException {
		Wss4jSecurityInterceptor subject = new Wss4jSecurityInterceptor();

		Transformer transformer = TransformerFactoryUtils.newInstance().newTransformer();

		SOAPMessage saajMessage = saajSoap11MessageFactory.createMessage();
		transformer.transform(new StringSource(PAYLOAD), new DOMResult(saajMessage.getSOAPBody()));
		SoapMessage message = new SaajSoapMessage(saajMessage, saajSoap11MessageFactory);
		MessageContext messageContext = new DefaultMessageContext(message, new SaajSoapMessageFactory(saajSoap11MessageFactory));

		RequestData validationData = ReflectionTestUtils.invokeMethod(subject, "initializeValidationRequestData", messageContext);

		assertTrue(validationData.isAddInclusivePrefixes());
	}


	@Test
	public void testThatInitializeValidationRequestDataSetsInclusivePrefixesUsingNotUsingInclusivePrefixes() throws TransformerException, SOAPException {
		Wss4jSecurityInterceptor subject = new Wss4jSecurityInterceptor();
		subject.setAddInclusivePrefixes(false);
				Transformer transformer = TransformerFactoryUtils.newInstance().newTransformer();

		SOAPMessage saajMessage = saajSoap11MessageFactory.createMessage();
		transformer.transform(new StringSource(PAYLOAD), new DOMResult(saajMessage.getSOAPBody()));
		SoapMessage message = new SaajSoapMessage(saajMessage, saajSoap11MessageFactory);
		MessageContext messageContext = new DefaultMessageContext(message, new SaajSoapMessageFactory(saajSoap11MessageFactory));

		RequestData validationData = ReflectionTestUtils.invokeMethod(subject, "initializeValidationRequestData", messageContext);

		assertFalse(validationData.isAddInclusivePrefixes());
	}
}