/*
 * Copyright (c) 2007, Your Corporation. All Rights Reserved.
 */

package org.springframework.ws.soap.addressing.messageid;

public class UuidMessageIdStrategyTest extends AbstractMessageIdStrategyTestCase {

    protected MessageIdStrategy createProvider() {
        return new UuidMessageIdStrategy();
    }

}