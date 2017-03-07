package org.springframework.ws.soap.client.core;

import java.io.IOException;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.soap.support.SoapUtils;
import org.springframework.ws.transport.TransportConstants;

public class SaajSoapActionCallbackTest {
	
	private SaajSoapMessageFactory saaj11Factory = new SaajSoapMessageFactory();
	
	private SaajSoapMessageFactory saaj12Factory = new SaajSoapMessageFactory();
	
	@Before
	public void init() throws SOAPException
	{
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
	public void noSoapAction11() throws IOException
	{
		SaajSoapMessage message = saaj11Factory.createWebServiceMessage();
		String[] soapActionHeaders = message.getSaajMessage().getMimeHeaders().getHeader(TransportConstants.HEADER_SOAP_ACTION);
		Assert.assertEquals("\"\"", soapActionHeaders[0]);
	}
	
	@Test
	public void soapAction11() throws IOException
	{
		SaajSoapMessage message = saaj11Factory.createWebServiceMessage();
		SoapActionCallback callback = new SoapActionCallback("testAction");
		callback.doWithMessage(message);
		String[] soapActionHeaders = message.getSaajMessage().getMimeHeaders().getHeader(TransportConstants.HEADER_SOAP_ACTION);
		Assert.assertEquals("\"testAction\"", soapActionHeaders[0]);
	}
	
	@Test
	public void emptySoapAction11() throws IOException
	{
		SaajSoapMessage message = saaj11Factory.createWebServiceMessage();
		SoapActionCallback callback = new SoapActionCallback(null);
		callback.doWithMessage(message);
		String[] soapActionHeaders = message.getSaajMessage().getMimeHeaders().getHeader(TransportConstants.HEADER_SOAP_ACTION);
		Assert.assertEquals("\"\"", soapActionHeaders[0]);
	}
	
	@Test
	public void noSoapAction12() throws IOException
	{
		SaajSoapMessage message = saaj12Factory.createWebServiceMessage();
		String[] soapActionHeaders = message.getSaajMessage().getMimeHeaders().getHeader(TransportConstants.HEADER_SOAP_ACTION);
		Assert.assertNull(soapActionHeaders);
	}
	
	@Test
	public void soapAction12() throws IOException
	{
		SaajSoapMessage message = saaj12Factory.createWebServiceMessage();
		SoapActionCallback callback = new SoapActionCallback("testAction");
		callback.doWithMessage(message);
		String[] soapActionHeaders = message.getSaajMessage().getMimeHeaders().getHeader(TransportConstants.HEADER_SOAP_ACTION);
		Assert.assertNull(soapActionHeaders);
		String[] contentTypes = message.getSaajMessage().getMimeHeaders().getHeader(TransportConstants.HEADER_CONTENT_TYPE);
		Assert.assertEquals("\"testAction\"", SoapUtils.extractActionFromContentType(contentTypes[0]));
	}
	
	@Test
	public void emptySoapAction12() throws IOException
	{
		SaajSoapMessage message = saaj12Factory.createWebServiceMessage();
		SoapActionCallback callback = new SoapActionCallback(null);
		callback.doWithMessage(message);
		String[] soapActionHeaders = message.getSaajMessage().getMimeHeaders().getHeader(TransportConstants.HEADER_SOAP_ACTION);
		Assert.assertNull(soapActionHeaders);
		String[] contentTypes = message.getSaajMessage().getMimeHeaders().getHeader(TransportConstants.HEADER_CONTENT_TYPE);
		Assert.assertEquals("\"\"", SoapUtils.extractActionFromContentType(contentTypes[0]));
	}

}
