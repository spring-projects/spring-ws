package org.springframework.ws.soap.addressing;

public class WsAddressing200508InterceptorTest extends AbstractWsAddressingInterceptorTestCase {

    protected AbstractWsAddressingInterceptor createInterceptor() {
        return new WsAddressing200508Interceptor();
    }

    protected String getTestPath() {
        return "200508";
    }
}