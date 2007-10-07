package org.springframework.ws.soap.addressing;

import java.io.IOException;
import javax.xml.transform.TransformerException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.addressing.messageid.MessageIdProvider;
import org.springframework.ws.soap.server.SoapEndpointInterceptor;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.WebServiceMessageSender;
import org.springframework.xml.transform.TransformerObjectSupport;

/** @author Arjen Poutsma */
public abstract class AbstractWsAddressingInterceptor extends TransformerObjectSupport
        implements SoapEndpointInterceptor {

    /** Logger available for subclasses. */
    protected final Log logger = LogFactory.getLog(getClass());

    private MessageIdProvider messageIdProvider;

    private WebServiceMessageSender[] messageSenders = new WebServiceMessageSender[0];

    public final void setMessageIdProvider(MessageIdProvider messageIdProvider) {
        this.messageIdProvider = messageIdProvider;
    }

    public final boolean handleRequest(MessageContext messageContext, Object endpoint) throws Exception {
        Assert.isTrue(messageContext.getRequest() instanceof SoapMessage,
                "WsAddressingInterceptor requires a SoapMessage request");
        SoapMessage request = (SoapMessage) messageContext.getRequest();
        MessageAddressingProperties requestMap = getMessageAddressingProperties(request);
        if (!requestMap.isValid()) {
            addMessageAddressingHeaderRequiredFault((SoapMessage) messageContext.getResponse());
            return false;
        }
        return true;
    }

    public final boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
        return handleResponseOrFault(messageContext);
    }

    public boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
        return handleResponseOrFault(messageContext);
    }

    /**
     * Adds a Message Addressing Header Required fault to the given message.
     *
     * @see <a href="http://www.w3.org/TR/ws-addr-soap/#missingmapfault">Message Addressing Header Required</a>
     */
    protected abstract SoapFault addMessageAddressingHeaderRequiredFault(SoapMessage message);

    private boolean handleResponseOrFault(MessageContext messageContext) throws Exception {
        Assert.isTrue(messageContext.getRequest() instanceof SoapMessage &&
                messageContext.getResponse() instanceof SoapMessage,
                "WsAddressingInterceptor requires a SoapMessage request and response");
        SoapMessage request = (SoapMessage) messageContext.getRequest();
        MessageAddressingProperties requestMap = getMessageAddressingProperties(request);
        SoapMessage response = (SoapMessage) messageContext.getResponse();
        EndpointReference responseEpr = response.hasFault() ? requestMap.getFaultTo() : requestMap.getReplyTo();
        if (responseEpr == null || hasNoneAddress(responseEpr)) {
            logger.debug("Request has none reply address");
            return false;
        }
        String responseMessageId = messageIdProvider.getMessageId(response);
        if (logger.isDebugEnabled()) {
            logger.debug("Generated reply MessageID [" + responseMessageId + "]");
        }
        MessageAddressingProperties replyMap = requestMap.getResponseProperties(responseEpr, null, responseMessageId);
        addAddressingHeaders(response, replyMap);
        if (hasAnonymousAddress(responseEpr)) {
            logger.debug("Request has anonymous reply address");
            return true;
        }
        else {
            if (logger.isDebugEnabled()) {
                logger.debug("Sending reply message to EPR address [" + responseEpr.getAddress() + "]");
            }
            sendOutOfBand(responseEpr.getAddress(), response);
            return false;
        }
    }

    private void sendOutOfBand(String uri, SoapMessage message) throws IOException {
        boolean supported = false;
        for (int i = 0; i < messageSenders.length; i++) {
            if (messageSenders[i].supports(uri)) {
                supported = true;
                WebServiceConnection connection = null;
                try {
                    connection = messageSenders[i].createConnection(uri);
                    connection.send(message);
                    break;
                }
                finally {
                    if (connection != null) {
                        connection.close();
                    }
                }
            }
        }
        if (!supported) {
            throw new IllegalArgumentException("Could not resolve [" + uri + "] to a WebServiceMessageSender");
        }
    }

    /**
     * Returns the {@link MessageAddressingProperties} for the given message.
     *
     * @param message the message to find the map for
     * @return the message addressing properties
     */
    protected abstract MessageAddressingProperties getMessageAddressingProperties(SoapMessage message)
            throws TransformerException;

    /**
     * Indicates whether the given endpoint reference has a None address. Messages to be sent to this address will not
     * be sent.
     *
     * @see <a href="http://www.w3.org/TR/ws-addr-core/#sendmsgepr">Sending a Message to an EPR</a>
     */
    protected abstract boolean hasNoneAddress(EndpointReference epr);

    /**
     * Indicates whether the given endpoint reference has a Anonymous address. This address is used to indicate that a
     * message should be sent in-band.
     *
     * @see <a href="http://www.w3.org/TR/ws-addr-core/#formreplymsg">Formulating a Reply Message</a>
     */
    protected abstract boolean hasAnonymousAddress(EndpointReference epr);

    protected abstract void addAddressingHeaders(SoapMessage message, MessageAddressingProperties map)
            throws TransformerException;

}
