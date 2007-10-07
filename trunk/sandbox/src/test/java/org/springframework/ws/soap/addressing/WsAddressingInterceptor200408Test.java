package org.springframework.ws.soap.addressing;

import org.springframework.ws.soap.addressing.version.WsAddressing200408;
import org.springframework.ws.soap.addressing.version.WsAddressingVersion;

public class WsAddressingInterceptor200408Test extends AbstractWsAddressingInterceptorTestCase {

    protected WsAddressingVersion getVersion() {
        return new WsAddressing200408();
    }

    protected String getTestPath() {
        return "200408";
    }

    public void testHandleNoneReplyTo() throws Exception {
        // This version of the spec does not have none addresses
    }
}
