/*
 * Copyright (c) 2007, Your Corporation. All Rights Reserved.
 */

package org.springframework.ws.soap.addressing.server;

import org.springframework.ws.soap.addressing.version.Addressing10;
import org.springframework.ws.soap.addressing.version.AddressingVersion;

public class AddressingInterceptor10Test extends AbstractAddressingInterceptorTestCase {

    protected AddressingVersion getVersion() {
        return new Addressing10();
    }

    protected String getTestPath() {
        return "10";
    }
}