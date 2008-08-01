/*
 * Copyright (c) 2007, Your Corporation. All Rights Reserved.
 */

package org.springframework.ws.soap.addressing.messageid;

import java.net.URI;

import junit.framework.TestCase;

public class UuidMessageIdStrategyTest extends TestCase {

    private MessageIdStrategy strategy;

    protected final void setUp() throws Exception {
        strategy = new UuidMessageIdStrategy();
    }

    public void testStrategy() {
        URI messageId1 = strategy.newMessageId(null);
        assertNotNull("Empty messageId", messageId1);
        URI messageId2 = strategy.newMessageId(null);
        assertNotNull("Empty messageId", messageId2);
        assertFalse("Equal messageIds", messageId1.equals(messageId2));
    }
}