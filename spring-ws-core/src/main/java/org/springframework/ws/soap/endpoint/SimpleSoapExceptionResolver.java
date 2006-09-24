package org.springframework.ws.soap.endpoint;

import java.util.Locale;

import org.springframework.util.StringUtils;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.endpoint.AbstractEndpointExceptionResolver;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.context.SoapMessageContext;

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
        if (!(messageContext instanceof SoapMessageContext)) {
            throw new IllegalArgumentException("SimpleSoapExceptionResolver requires a SoapMessageContext");
        }
        String faultString = StringUtils.hasLength(ex.getMessage()) ? ex.getMessage() : ex.toString();
        SoapMessageContext soapContext = (SoapMessageContext) messageContext;
        SoapBody body = soapContext.getSoapResponse().getSoapBody();
        body.addServerOrReceiverFault(faultString, locale);
        return true;
    }
}
