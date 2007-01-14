package org.springframework.ws.soap.endpoint;

import java.util.Locale;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.AbstractEndpointExceptionResolver;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapMessage;

/**
 * Simple, SOAP-specific implementation of the <code>EndpointExceptionResolver</code> that stores the exception's
 * message as the fault string. The fault code is always set to a Sender (in SOAP 1.1) or Receiver (SOAP 1.2).
 *
 * @author Arjen Poutsma
 */
public class SimpleSoapExceptionResolver extends AbstractEndpointExceptionResolver {

    private Locale locale = Locale.ENGLISH;

    /**
     * Sets the locale for the faultstring or reason of the SOAP Fault. Defaults to english.
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    protected boolean resolveExceptionInternal(MessageContext messageContext, Object endpoint, Exception ex) {
        Assert.isTrue(messageContext.getResponse() instanceof SoapMessage,
                "SimpleSoapExceptionResolver requires a SoapMessage");
        SoapMessage response = (SoapMessage) messageContext.getResponse();
        String faultString = StringUtils.hasLength(ex.getMessage()) ? ex.getMessage() : ex.toString();
        SoapBody body = response.getSoapBody();
        body.addServerOrReceiverFault(faultString, locale);
        return true;
    }
}
