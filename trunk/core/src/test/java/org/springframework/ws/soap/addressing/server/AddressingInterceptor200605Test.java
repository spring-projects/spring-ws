/*
 * Copyright (c) 2007, Your Corporation. All Rights Reserved.
 */

package org.springframework.ws.soap.addressing.server;

import org.springframework.ws.soap.addressing.version.WsAddressing200605;
import org.springframework.ws.soap.addressing.version.WsAddressingVersion;

public class AddressingInterceptor200605Test extends AbstractAddressingInterceptorTestCase {

    protected WsAddressingVersion getVersion() {
        return new WsAddressing200605();
    }

    protected String getTestPath() {
        return "200605";
    }
}