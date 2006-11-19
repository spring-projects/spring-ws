package org.springframework.ws.transport;

import java.io.IOException;

import org.springframework.ws.context.MessageContext;

/**
 * @author Arjen Poutsma
 */
public interface MessageSender {

    /**
     * Sends the given message context. The response message, if any, is stored in the context.
     *
     * @param messageContext the message to be sent
     */
    void send(MessageContext messageContext) throws IOException;

}
