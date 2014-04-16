package org.springframework.ws.transport.context;

import org.springframework.ws.transport.WebServiceConnection;

/**
 * Strategy interface for determining the current {@link org.springframework.ws.transport.WebServiceConnection}.
 *
 * <p>An instance of this class can be associated with a thread via the {@link TransportContextHolder} class.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public interface TransportContext {

    /** Returns the current {@code WebServiceConnection}. */
    WebServiceConnection getConnection();
}
