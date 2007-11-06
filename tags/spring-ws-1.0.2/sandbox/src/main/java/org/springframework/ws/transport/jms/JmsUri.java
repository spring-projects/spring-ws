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

import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Topic;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.transport.support.ParameterizedUri;

/**
 * @author Arjen Poutsma
 * @see <a href="http://mail-archives.apache.org/mod_mbox/ws-axis-dev/200701.mbox/raw/%3C80A43FC052CE3949A327527DCD5D6B27020FB65C@MAIL01.bedford.progress.com%3E/2">RI
 *      Scheme for Java Message Service 1.0 RC1</a>
 */
public class JmsUri extends ParameterizedUri implements JmsTransportConstants {

    public JmsUri(String uri) {
        super(uri);
        validateParameters();
    }

    private void validateParameters() {
        validateIntegerParameter(PARAM_DELIVERY_MODE);
        validateIntegerParameter(PARAM_PRIORITY);
        validateIntegerParameter(PARAM_TIME_TO_LIVE);
        String destinationType = getDestinationType();
        if (StringUtils.hasLength(destinationType)) {
            Assert.isTrue(
                    DESTINATION_TYPE_QUEUE.equals(destinationType) || DESTINATION_TYPE_TOPIC.equals(destinationType),
                    "Invalid " + PARAM_DESTINATION_TYPE + ": [" + destinationType + "]. Expected '" +
                            DESTINATION_TYPE_QUEUE + "' or '" + DESTINATION_TYPE_TOPIC + "'");
        }
    }

    private void validateIntegerParameter(String paramName) {
        String paramValue = getParameter(paramName);
        if (StringUtils.hasLength(paramValue)) {
            try {
                Integer.parseInt(paramValue);
            }
            catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Invalid " + paramName + ": [" + paramValue + "]. Not an integer.");
            }
        }
    }

    /**
     * Returns whether the request message is persistent or not.
     *
     * @see DeliveryMode#NON_PERSISTENT
     * @see DeliveryMode#PERSISTENT
     */
    public int getDeliveryMode() {
        return getIntegerParameter(PARAM_DELIVERY_MODE, Message.DEFAULT_DELIVERY_MODE);
    }

    public String getDestination() {
        return super.getDestination();
    }

    /**
     * Specifies whether the destination is a {@link Queue} or a {@link Topic}, with the value "<code>queue</code>" or
     * "<code>topic</code>", respectively.
     */
    public String getDestinationType() {
        return getParameter(PARAM_DESTINATION_TYPE);
    }

    /**
     * Returns the JMS priority associated with the request message.
     *
     * @see Message#setJMSPriority(int)
     */
    public int getPriority() {
        return getIntegerParameter(PARAM_PRIORITY, Message.DEFAULT_PRIORITY);
    }

    /**
     * Returns the lifetime, in milliseconds, of the request message.
     */
    public long getTimeToLive() {
        String paramValue = getParameter(PARAM_TIME_TO_LIVE);
        return paramValue != null ? Long.parseLong(paramValue) : Message.DEFAULT_TIME_TO_LIVE;
    }

    private int getIntegerParameter(String paramName, int defaultValue) {
        String paramValue = getParameter(paramName);
        return paramValue != null ? Integer.parseInt(paramValue) : defaultValue;
    }

    /**
     * Indicates whether this URI has a reply-to name.
     */
    public boolean hasReplyTo() {
        return StringUtils.hasLength(getReplyTo());
    }

    /**
     * Returns the reply-to name.
     *
     * @see Message#setJMSReplyTo(Destination)
     */
    public String getReplyTo() {
        return getParameter(PARAM_REPLY_TO_NAME);
    }

    /**
     * Return whether the Publish/Subscribe domain ({@link javax.jms.Topic Topics}) is used. Otherwise, the
     * Point-to-Point domain ({@link javax.jms.Queue Queues}) is used.
     */
    public boolean isPubSubDomain() {
        return DESTINATION_TYPE_TOPIC.equals(getDestinationType());
    }

}
