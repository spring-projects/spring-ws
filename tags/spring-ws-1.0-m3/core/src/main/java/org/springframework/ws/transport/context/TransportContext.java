package org.springframework.ws.transport.context;

import org.springframework.ws.transport.TransportInputStream;
import org.springframework.ws.transport.TransportOutputStream;

/**
 * Strategy interface for determining the current {@link org.springframework.ws.transport.TransportInputStream} and
 * {@link org.springframework.ws.transport.TransportOutputStream}.
 * <p/>
 * An instance of this class can be associated with a thread via the {@link TransportContextHolder} class.
 *
 * @author Arjen Poutsma
 */
public interface TransportContext {

    /**
     * Returns the current <code>TransportInputStream</code>.
     */
    TransportInputStream getTransportInputStream();

    /**
     * Returns the current <code>TransportOutputStream</code>.
     */
    TransportOutputStream getTransportOutputStream();
}
