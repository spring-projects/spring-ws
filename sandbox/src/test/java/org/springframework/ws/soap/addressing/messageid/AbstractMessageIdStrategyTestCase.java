/*
 * Copyright (c) 2007, Your Corporation. All Rights Reserved.
 */

package org.springframework.ws.soap.addressing.messageid;

import junit.framework.TestCase;
import org.springframework.util.StringUtils;

public abstract class AbstractMessageIdStrategyTestCase extends TestCase {

    private MessageIdStrategy strategy;

    protected final void setUp() throws Exception {
        strategy = createProvider();
    }

    protected abstract MessageIdStrategy createProvider();

    public void testProvider() {
        String messageId1 = strategy.newMessageId(null);
        assertTrue("Empty messageId", StringUtils.hasLength(messageId1));
        String messageId2 = strategy.newMessageId(null);
        assertTrue("Empty messageId", StringUtils.hasLength(messageId2));
        assertFalse("Equal messageIds", messageId1.equals(messageId2));
    }
}
