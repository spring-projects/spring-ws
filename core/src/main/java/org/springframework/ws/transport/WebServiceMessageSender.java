package org.springframework.ws.transport;

import java.io.IOException;

import org.springframework.ws.context.MessageContext;

/**
 * Defines the methods for classes capable of sending and receiving {@link org.springframework.ws.WebServiceMessage}
 * instances across a transport.
 *
 * @author Arjen Poutsma
 */
public interface WebServiceMessageSender {

    /**
     * Sends the given message context. The response message, if any, is stored in the context.
     *
     * @param messageContext the message to be sent
     * @throws IOException in case of I/O errors
     */
    void sendAndReceive(MessageContext messageContext) throws IOException;

}
