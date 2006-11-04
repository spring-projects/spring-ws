package org.springframework.ws.transport;

import org.springframework.ws.WebServiceMessage;

/**
 * @author Arjen Poutsma
 */
public interface WebServiceMessageSender {

    /**
     * Sends the given web service message and returns the result, if any.
     *
     * @param message the message to be sent
     * @return the result, or <code>null</code> if none
     */
    WebServiceMessage send(WebServiceMessage message);

}
