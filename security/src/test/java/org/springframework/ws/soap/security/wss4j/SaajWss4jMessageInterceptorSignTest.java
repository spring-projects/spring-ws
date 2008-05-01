package org.springframework.ws.soap.security.wss4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPMessage;

import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;

public class SaajWss4jMessageInterceptorSignTest extends Wss4jMessageInterceptorSignTestCase {

    public void testSignAndValidate() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        interceptor.setSecurementActions("Signature");
        interceptor.setEnableSignatureConfirmation(false);
        interceptor.setSecurementPassword("123456");
        interceptor.setSecurementUsername("rsaKey");
        SOAPMessage saajMessage = messageFactory.createMessage();
        saajMessage.getSOAPBody()
                .addBodyElement(new QName("http://fabrikam123.com/payloads", "StockSymbol", "tru")).addTextNode("QQQ");
        SoapMessage message = new SaajSoapMessage(saajMessage);
        MessageContext messageContext = new DefaultMessageContext(message, new SaajSoapMessageFactory(messageFactory));

        interceptor.secureMessage(message, messageContext);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        message.writeTo(bos);

        MimeHeaders mimeHeaders = new MimeHeaders();
        mimeHeaders.addHeader("Content-Type", "text/xml");
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());

        SOAPMessage signed = messageFactory.createMessage(mimeHeaders, bis);
        message = new SaajSoapMessage(signed);
        messageContext = new DefaultMessageContext(message, new SaajSoapMessageFactory(messageFactory));

        interceptor.validateMessage(message, messageContext);
    }

}
