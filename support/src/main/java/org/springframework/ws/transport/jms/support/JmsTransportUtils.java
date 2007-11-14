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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.Message;

/**
 * Collection of utility methods to work with JMS transports. Includes methods to retrieve JMS properties from an {@link
 * URI}.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public class JmsTransportUtils {

    private static final Pattern DESTINATION_NAME_PATTERN = Pattern.compile("^([^\\?]+)");

    private static final Pattern DELIVERY_MODE_PATTERN = Pattern.compile("deliveryMode=(PERSISTENT|NON_PERSISTENT)");

    private static final Pattern TIME_TO_LIVE_PATTERN = Pattern.compile("timeToLive=(\\d+)");

    private static final Pattern PRIORITY_PATTERN = Pattern.compile("priority=(\\d)");

    private static final Pattern REPLY_TO_NAME_PATTERN = Pattern.compile("replyToName=(\\w+)");

    private JmsTransportUtils() {
    }

    public static String getDestinationName(URI uri) {
        return getStringParameter(DESTINATION_NAME_PATTERN, uri);
    }

    /**
     * Returns the delivery mode of the given URI.
     *
     * @see DeliveryMode#NON_PERSISTENT
     * @see DeliveryMode#PERSISTENT
     * @see Message#DEFAULT_DELIVERY_MODE
     */
    public static int getDeliveryMode(URI uri) {
        String deliveryMode = getStringParameter(DELIVERY_MODE_PATTERN, uri);
        if ("NON_PERSISTENT".equals(deliveryMode)) {
            return DeliveryMode.NON_PERSISTENT;
        }
        else if ("PERSISTENT".equals(deliveryMode)) {
            return DeliveryMode.PERSISTENT;
        }
        else {
            return Message.DEFAULT_DELIVERY_MODE;
        }
    }

    /**
     * Returns the lifetime, in milliseconds, of the given URI.
     *
     * @see Message#DEFAULT_TIME_TO_LIVE
     */
    public static long getTimeToLive(URI uri) {
        return getLongParameter(TIME_TO_LIVE_PATTERN, uri, Message.DEFAULT_TIME_TO_LIVE);
    }

    /**
     * Returns the priority of the given URI.
     *
     * @see Message#DEFAULT_PRIORITY
     */
    public static int getPriority(URI uri) {
        return getIntParameter(PRIORITY_PATTERN, uri, Message.DEFAULT_PRIORITY);
    }

    /**
     * Returns the reply-to name of the given URI.
     *
     * @see Message#setJMSReplyTo(Destination)
     */
    public static String getReplyToName(URI uri) {
        return getStringParameter(REPLY_TO_NAME_PATTERN, uri);
    }

    private static String getStringParameter(Pattern pattern, URI uri) {
        Matcher matcher = pattern.matcher(uri.getSchemeSpecificPart());
        if (matcher.find() && matcher.groupCount() == 1) {
            return matcher.group(1);
        }
        return null;
    }

    private static int getIntParameter(Pattern pattern, URI uri, int defaultValue) {
        Matcher matcher = pattern.matcher(uri.getSchemeSpecificPart());
        if (matcher.find() && matcher.groupCount() == 1) {
            try {
                return Integer.parseInt(matcher.group(1));
            }
            catch (NumberFormatException ex) {
                // fall through to default value
            }
        }
        return defaultValue;
    }

    private static long getLongParameter(Pattern pattern, URI uri, long defaultValue) {
        Matcher matcher = pattern.matcher(uri.getSchemeSpecificPart());
        if (matcher.find() && matcher.groupCount() == 1) {
            try {
                return Long.parseLong(matcher.group(1));
            }
            catch (NumberFormatException ex) {
                // fall through to default value
            }
        }
        return defaultValue;
    }

}
