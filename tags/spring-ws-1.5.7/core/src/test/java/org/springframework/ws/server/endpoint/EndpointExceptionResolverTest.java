package org.springframework.ws.server.endpoint;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.springframework.ws.context.MessageContext;

/**
 * Test for AbstractEndpointExceptionResolver
 *
 * @author Tareq Abed Rabbo
 * @author Arjen Poutsma
 */
public class EndpointExceptionResolverTest extends TestCase {

    private MethodEndpoint methodEndpoint;

    private AbstractEndpointExceptionResolver exceptionResolver;

    protected void setUp() throws Exception {
        exceptionResolver = new AbstractEndpointExceptionResolver() {

            protected boolean resolveExceptionInternal(MessageContext messageContext, Object endpoint, Exception ex) {
                return true;
            }
        };

        Set mappedEndpoints = new HashSet();
        mappedEndpoints.add(this);
        exceptionResolver.setMappedEndpoints(mappedEndpoints);
        methodEndpoint = new MethodEndpoint(this, getClass().getMethod("emptyMethod", new Class[0]));
    }

    public void testMatchMethodEndpoint() {
        boolean matched = exceptionResolver.resolveException(null, methodEndpoint, null);
        assertTrue("AbstractEndpointExceptionResolver did not match mapped MethodEndpoint", matched);
    }

    public void emptyMethod() {
    }
}
