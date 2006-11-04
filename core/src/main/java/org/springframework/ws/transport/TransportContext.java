package org.springframework.ws.transport;

/**
 * Strategy interface for determining the current {@link TransportInputStream} and {@link TransportOutputStream}.
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
