package org.springframework.ws.client.support.interceptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.context.MessageContext;

/**
 *
 * Default implementation of the {@code ClientInterceptor} interface, for simplified implementation of
 * pre-only/post-only interceptors.
 *
 * @author Marten Deinum
 * @since 2.2.5
 */
public abstract class ClientInterceptorAdapter implements ClientInterceptor {

    /** Logger available to subclasses */
    protected final Log logger = LogFactory.getLog(getClass());

    @Override
    public boolean handleRequest(MessageContext messageContext) throws WebServiceClientException {
        return true;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) throws WebServiceClientException {
        return true;
    }

    @Override
    public boolean handleFault(MessageContext messageContext) throws WebServiceClientException {
        return true;
    }

    /**
     * Does nothing by default.
     */
    @Override
    public void afterCompletion(MessageContext messageContext, Exception ex) throws WebServiceClientException {

    }
}
