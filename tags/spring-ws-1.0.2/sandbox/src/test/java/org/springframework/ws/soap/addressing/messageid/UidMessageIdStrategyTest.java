/*
 * Copyright (c) 2007, Your Corporation. All Rights Reserved.
 */

package org.springframework.ws.soap.addressing.messageid;

public class UidMessageIdStrategyTest extends AbstractMessageIdStrategyTestCase {

    protected MessageIdStrategy createProvider() {
        return new UidMessageIdStrategy();
    }
}