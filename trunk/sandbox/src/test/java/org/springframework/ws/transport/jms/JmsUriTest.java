/*
 * Copyright 2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.transport.jms;

import junit.framework.TestCase;

public class JmsUriTest extends TestCase {

    public void testJmsUri() {
        JmsUri uri = new JmsUri("jms:news?connectionFactoryName=SOAPJMSFactory&" + "deliveryMode=2&" +
                "destinationType=topic&" + "initialContextFactory=com.sun.jndi.ldap.LdapCtxFactory&" +
                "jndiURL=theJndiURL&" + "priority=8&" + "timeToLive=10&" + "replyToName=interested&" +
                "userprop=mystuff");
        assertEquals("Invalid connection factory name", "SOAPJMSFactory", uri.getConnectionFactoryName());
        assertEquals("Invalid delivery mode", 2, uri.getDeliveryMode());
        assertEquals("Invalid destination", "news", uri.getDestination());
        assertEquals("Invalid destination type", "topic", uri.getDestinationType());
        assertTrue("Invalid pub sub domain", uri.isPubSubDomain());
        assertEquals("Invalid initial context factory", "com.sun.jndi.ldap.LdapCtxFactory",
                uri.getInitialContextFactory());
        assertEquals("Invalid prority", 8, uri.getPriority());
        assertEquals("Invalid time to live", 10, uri.getTimeToLive());
        assertEquals("Invalid reply to name", "interested", uri.getReplyTo());
        assertEquals("Invalid custom property", "mystuff", uri.getCustomParameter("userprop"));

    }

    public void testGetDestinationNoParams() {
        JmsUri uri = new JmsUri("jms:news");
        assertEquals("Invalid destination", "news", uri.getDestination());
    }

    public void testInvalidDeliveryMode() {
        testIllegalArgument("jms:news?deliveryMode=abc");
    }

    public void testInvalidPriority() {
        testIllegalArgument("jms:news?priority=abc");
    }

    public void testInvalidTimeToLive() {
        testIllegalArgument("jms:news?timeToLive=abc");
    }

    public void testInvalidDestinationType() {
        testIllegalArgument("jms:news?destinationType=abc");
    }

    public void testEmpty() {
        testIllegalArgument("");
    }

    public void testInvalidScheme() {
        testIllegalArgument("http://localhost");
    }

    public void testNoDestination() {
        testIllegalArgument("jms:");
    }

    public void testIllegalParam() {
        testIllegalArgument("jms:news?bla");
    }

    private void testIllegalArgument(String uri) {
        try {
            new JmsUri(uri);
            fail("Expected IllegalArgumentException for uri [" + uri + "]");
        }
        catch (IllegalArgumentException ex) {
            //expected
        }
    }
}