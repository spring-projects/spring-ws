package org.springframework.ws.soap.addressing;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.addressing.messageid.MessageIdStrategy;
import org.springframework.ws.soap.addressing.version.WsAddressingVersion;
import org.springframework.ws.soap.server.SoapEndpointInterceptor;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.WebServiceMessageSender;

/**
 * {@link SoapEndpointInterceptor} implementation that u
 *
 * @author Arjen Poutsma
 */
class WsAddressingInterceptor implements SoapEndpointInterceptor {

    private static final Log logger = LogFactory.getLog(WsAddressingInterceptor.class);

    private final WsAddressingVersion version;

    private final MessageIdStrategy messageIdStrategy;

    private final WebServiceMessageSender[] messageSenders;

    WsAddressingInterceptor(WsAddressingVersion version,
                            MessageIdStrategy messageIdStrategy,
                            WebServiceMessageSender[] messageSenders) {
        Assert.notNull(version, "version must not be null");
        Assert.notNull(messageIdStrategy, "messageIdStrategy must not be null");
        Assert.notNull(messageSenders, "messageSenders must not be null");
        this.version = version;
        this.messageIdStrategy = messageIdStrategy;
        this.messageSenders = messageSenders;
    }

    public boolean handleRequest(MessageContext messageContext, Object endpoint) throws Exception {
        Assert.isInstanceOf(SoapMessage.class, messageContext.getRequest(),
                "WsAddressingInterceptor requires a SoapMessage request");
        SoapMessage request = (SoapMessage) messageContext.getRequest();
        MessageAddressingProperties requestMap = version.getMessageAddressingProperties(request);
        if (!requestMap.hasRequiredProperties()) {
            version.addMessageAddressingHeaderRequiredFault((SoapMessage) messageContext.getResponse());
            return false;
        }
        if (!requestMap.isValid() || messageIdStrategy.isDuplicate(requestMap.getMessageId())) {
            version.addInvalidAddressingHeaderFault((SoapMessage) messageContext.getResponse());
            return false;
        }
        return true;
    }

    public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
        return handleResponseOrFault(messageContext);
    }

    public boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
        return handleResponseOrFault(messageContext);
    }

    private boolean handleResponseOrFault(MessageContext messageContext) throws Exception {
        Assert.isInstanceOf(SoapMessage.class, messageContext.getRequest(),
                "WsAddressingInterceptor requires a SoapMessage request");
        Assert.isInstanceOf(SoapMessage.class, messageContext.getResponse(),
                "WsAddressingInterceptor requires a SoapMessage response");
        SoapMessage request = (SoapMessage) messageContext.getRequest();
        MessageAddressingProperties requestMap = version.getMessageAddressingProperties(request);
        SoapMessage response = (SoapMessage) messageContext.getResponse();
        EndpointReference responseEpr = response.hasFault() ? requestMap.getFaultTo() : requestMap.getReplyTo();
        if (responseEpr == null || version.hasNoneAddress(responseEpr)) {
            logger.debug("Request has none reply address");
            return false;
        }
        String responseMessageId = messageIdStrategy.newMessageId(response);
        if (logger.isDebugEnabled()) {
            logger.debug("Generated reply MessageID [" + responseMessageId + "]");
        }
        MessageAddressingProperties replyMap = requestMap.getResponseProperties(responseEpr, null, responseMessageId);
        version.addAddressingHeaders(response, replyMap);
        if (version.hasAnonymousAddress(responseEpr)) {
            logger.debug("Sending in-band reply");
            return true;
        }
        else {
            if (logger.isDebugEnabled()) {
                logger.debug("Sending out-of-band reply message to EPR address [" + responseEpr.getAddress() + "]");
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
            logger.warn("Could not send out-of-band response to [" + uri + "]. " +
                    "Configure WebServiceMessageSenders which support this uri.");
        }
    }

    public boolean understands(SoapHeaderElement header) {
        return version.understands(header);
    }
}
