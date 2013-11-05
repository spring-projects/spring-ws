/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.soap.security.wss4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import javax.xml.namespace.QName;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;

import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.xml.transform.StringSource;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SaajWss4jMessageInterceptorSignTest extends Wss4jMessageInterceptorSignTestCase {

    private static final String PAYLOAD =
            "<tru:StockSymbol xmlns:tru=\"http://fabrikam123.com/payloads\">QQQ</tru:StockSymbol>";

    @Test
    public void testSignAndValidate() throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        interceptor.setSecurementActions("Signature");
        interceptor.setEnableSignatureConfirmation(false);
        interceptor.setSecurementPassword("123456");
        interceptor.setSecurementUsername("rsaKey");
        SOAPMessage saajMessage = saajSoap11MessageFactory.createMessage();
        transformer.transform(new StringSource(PAYLOAD), new DOMResult(saajMessage.getSOAPBody()));
        SoapMessage message = new SaajSoapMessage(saajMessage, saajSoap11MessageFactory);
        MessageContext messageContext = new DefaultMessageContext(message, new SaajSoapMessageFactory(saajSoap11MessageFactory));

        interceptor.secureMessage(message, messageContext);

        SOAPHeader header = ((SaajSoapMessage) message).getSaajMessage().getSOAPHeader();
        Iterator<?> iterator = header.getChildElements(new QName(
                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Security"));
        assertTrue("No security header", iterator.hasNext());
        SOAPHeaderElement securityHeader = (SOAPHeaderElement) iterator.next();
        iterator = securityHeader.getChildElements(new QName("http://www.w3.org/2000/09/xmldsig#", "Signature"));
        assertTrue("No signature header", iterator.hasNext());

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        message.writeTo(bos);

        MimeHeaders mimeHeaders = new MimeHeaders();
        mimeHeaders.addHeader("Content-Type", "text/xml");
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());

        SOAPMessage signed = saajSoap11MessageFactory.createMessage(mimeHeaders, bis);
        message = new SaajSoapMessage(signed, saajSoap11MessageFactory);
        messageContext = new DefaultMessageContext(message, new SaajSoapMessageFactory(saajSoap11MessageFactory));

        interceptor.validateMessage(message, messageContext);
    }

}
