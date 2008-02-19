/*
 * Copyright (c) 2007, Your Corporation. All Rights Reserved.
 */

package org.springframework.ws.soap.addressing;

public class WsAddressingInterceptor200605Test extends AbstractWsAddressingInterceptorTestCase {

    protected WsAddressingVersion getVersion() {
        return new WsAddressing200605();
    }

    protected String getTestPath() {
        return "200605";
    }
}