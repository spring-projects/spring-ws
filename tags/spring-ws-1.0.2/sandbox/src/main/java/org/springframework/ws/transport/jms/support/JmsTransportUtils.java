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

import org.springframework.ws.transport.jms.JmsTransportConstants;

/**
 * Collection of utility methods to work with JMS transports. Includes methods to convert from transport header names to
 * JMS Properties and vice-versa.
 *
 * @author Arjen Poutsma
 * @since 1.1.0
 */
public class JmsTransportUtils {

    private static final String[] CONVERSION_TABLE = new String[]{JmsTransportConstants.HEADER_CONTENT_TYPE,
            JmsTransportConstants.PROPERTY_CONTENT_TYPE, JmsTransportConstants.HEADER_CONTENT_LENGTH,
            JmsTransportConstants.PROPERTY_CONTENT_LENGTH, JmsTransportConstants.HEADER_SOAP_ACTION,
            JmsTransportConstants.PROPERTY_SOAP_ACTION};

    private JmsTransportUtils() {
    }

    /**
     * Converts the given transport header to a JMS property name. Returns the given header name if no match is found.
     *
     * @param headerName the header name to transform
     * @return the JMS property name
     */
    public static String headerToJmsProperty(String headerName) {
        for (int i = 0; i < CONVERSION_TABLE.length; i = i + 2) {
            if (CONVERSION_TABLE[i].equals(headerName)) {
                return CONVERSION_TABLE[i + 1];
            }
        }
        return headerName;
    }

    /**
     * Converts the given JMS property name to a transport header name. Returns the given property name if no match is
     * found.
     *
     * @param propertyName the JMS property name to transform
     * @return the transport header name
     */
    public static String jmsPropertyToHeader(String propertyName) {
        for (int i = 1; i < CONVERSION_TABLE.length; i = i + 2) {
            if (CONVERSION_TABLE[i].equals(propertyName)) {
                return CONVERSION_TABLE[i - 1];
            }
        }
        return propertyName;
    }

}
