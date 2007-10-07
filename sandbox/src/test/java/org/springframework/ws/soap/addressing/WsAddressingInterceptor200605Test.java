package org.springframework.ws.soap.addressing;

import org.springframework.ws.soap.addressing.version.WsAddressing200605;
import org.springframework.ws.soap.addressing.version.WsAddressingVersion;

public class WsAddressingInterceptor200605Test extends AbstractWsAddressingInterceptorTestCase {

    protected WsAddressingVersion getVersion() {
        return new WsAddressing200605();
    }

    protected String getTestPath() {
        return "200508";
    }
}