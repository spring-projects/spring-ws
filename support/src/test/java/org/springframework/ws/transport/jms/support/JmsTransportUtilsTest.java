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

package org.springframework.ws.transport.jms.support;

import java.net.URI;
import javax.jms.DeliveryMode;
import javax.jms.Message;

import junit.framework.TestCase;

public class JmsTransportUtilsTest extends TestCase {

    public void testHeaderToJmsProperty() throws Exception {
        String result = JmsTransportUtils.headerToJmsProperty("SOAPAction");
        assertEquals("Invalid result", "SOAPJMS_soapAction", result);
    }

    public void testGetDeliveryMode() throws Exception {
        URI uri = new URI("jms:RequestQueue?deliveryMode=NON_PERSISTENT");
        int deliveryMode = JmsTransportUtils.getDeliveryMode(uri);
        assertEquals("Invalid deliveryMode", DeliveryMode.NON_PERSISTENT, deliveryMode);

        uri = new URI("jms:RequestQueue?deliveryMode=PERSISTENT");
        deliveryMode = JmsTransportUtils.getDeliveryMode(uri);
        assertEquals("Invalid deliveryMode", DeliveryMode.PERSISTENT, deliveryMode);

        uri = new URI("jms:RequestQueue?replyToName=RESP_QUEUE");
        deliveryMode = JmsTransportUtils.getDeliveryMode(uri);
        assertEquals("Invalid deliveryMode", Message.DEFAULT_DELIVERY_MODE, deliveryMode);
    }

    public void testGetTimeToLive() throws Exception {
        URI uri = new URI("jms:RequestQueue?timeToLive=100");
        long timeToLive = JmsTransportUtils.getTimeToLive(uri);
        assertEquals("Invalid timeToLive", 100, timeToLive);

        uri = new URI("jms:RequestQueue?replyToName=RESP_QUEUE");
        timeToLive = JmsTransportUtils.getTimeToLive(uri);
        assertEquals("Invalid timeToLive", Message.DEFAULT_TIME_TO_LIVE, timeToLive);
    }

    public void testGetPriority() throws Exception {
        URI uri = new URI("jms:RequestQueue?priority=5");
        int priority = JmsTransportUtils.getPriority(uri);
        assertEquals("Invalid priority", 5, priority);

        uri = new URI("jms:RequestQueue?replyToName=RESP_QUEUE");
        priority = JmsTransportUtils.getPriority(uri);
        assertEquals("Invalid priority", Message.DEFAULT_PRIORITY, priority);
    }

    public void testGetReplyToName() throws Exception {
        URI uri = new URI("jms:RequestQueue?replyToName=RESP_QUEUE");
        String replyToName = JmsTransportUtils.getReplyToName(uri);
        assertEquals("Invalid replyToName", "RESP_QUEUE", replyToName);

        uri = new URI("jms:RequestQueue?priority=5");
        replyToName = JmsTransportUtils.getReplyToName(uri);
        assertNull("Invalid replyToName", replyToName);
    }

    public void testGetDestinationName() throws Exception {
        URI uri = new URI("jms:RequestQueue?replyToName=RESP_QUEUE");
        String destinationName = JmsTransportUtils.getDestinationName(uri);
        assertEquals("Invalid destination", "RequestQueue", destinationName);

        uri = new URI("jms:RequestQueue");
        destinationName = JmsTransportUtils.getDestinationName(uri);
        assertEquals("Invalid destination", "RequestQueue", destinationName);
    }
}