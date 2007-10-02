package org.springframework.ws.soap.addressing;

public class WsAddressing200408InterceptorTest extends AbstractWsAddressingInterceptorTestCase {

    protected AbstractWsAddressingInterceptor createInterceptor() {
        return new WsAddressing200408Interceptor();
    }

    protected String getTestPath() {
        return "200408";
    }

    public void testHandleNoneReplyTo() throws Exception {
        // This version of the spec does not have none addresses
    }
}
