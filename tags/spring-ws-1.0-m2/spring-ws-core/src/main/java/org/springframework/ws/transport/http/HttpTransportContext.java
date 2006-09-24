package org.springframework.ws.transport.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.Assert;
import org.springframework.ws.transport.TransportContext;
import org.springframework.ws.transport.TransportRequest;
import org.springframework.ws.transport.TransportResponse;

/**
 * HTTP-specific implementation of the <code>TransportContext</code> interface. Exposes the
 * <code>HttpServletRequest</code> and <code>HttpServletResponse</code>.
 *
 * @author Arjen Poutsma
 */
public class HttpTransportContext implements TransportContext {

    private final HttpTransportRequest transportRequest;

    private final HttpTransportResponse transportResponse;

    /**
     * Constructs a new instance of the <code>HttpTransportContext</code> with the given <code>HttpServletRequest</code>
     * and <code>HttpServletResponse</code>
     */
    public HttpTransportContext(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        Assert.notNull(httpServletRequest, "No httpServletRequest given");
        Assert.notNull(httpServletResponse, "No httpServletResponse given");
        transportRequest = new HttpTransportRequest(httpServletRequest);
        transportResponse = new HttpTransportResponse(httpServletResponse);
    }

    public TransportRequest getTransportRequest() {
        return transportRequest;
    }

    public TransportResponse getTransportResponse() {
        return transportResponse;
    }

    /**
     * Returns the wrapped <code>HttpServletRequest</code>.
     */
    public HttpServletRequest getHttpServletRequest() {
        return transportRequest.getHttpServletRequest();
    }

    /**
     * Returns the wrapped <code>HttpServletResponse</code>.
     */
    public HttpServletResponse getHttpServletRespo() {
        return transportResponse.getHttpServletResponse();
    }

}
