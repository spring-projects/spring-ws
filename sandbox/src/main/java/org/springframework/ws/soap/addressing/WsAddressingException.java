package org.springframework.ws.soap.addressing;

import org.springframework.ws.WebServiceException;

/**
 * Exception thrown in cases on WS-Addressing errors.
 *
 * @author Arjen Poutsma
 * @since 1.1.0
 */
public class WsAddressingException extends WebServiceException {

    public WsAddressingException(String msg) {
        super(msg);
    }

    public WsAddressingException(String msg, Throwable ex) {
        super(msg, ex);
    }
}
