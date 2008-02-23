/*
 * Copyright (c) 2007, Your Corporation. All Rights Reserved.
 */

package org.springframework.ws.soap.addressing.server;

import org.springframework.ws.soap.addressing.version.WsAddressing200408;
import org.springframework.ws.soap.addressing.version.WsAddressingVersion;

public class AddressingInterceptor200408Test extends AbstractAddressingInterceptorTestCase {

    protected WsAddressingVersion getVersion() {
        return new WsAddressing200408();
    }

    protected String getTestPath() {
        return "200408";
    }

    public void testNoneReplyTo() throws Exception {
        // This version of the spec does not have none addresses
    }
}
