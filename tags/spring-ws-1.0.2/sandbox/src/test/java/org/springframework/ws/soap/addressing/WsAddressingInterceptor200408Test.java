/*
 * Copyright (c) 2007, Your Corporation. All Rights Reserved.
 */

package org.springframework.ws.soap.addressing;

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
