/*
 * Copyright 2008 the original author or authors.
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

package org.springframework.ws.soap.support;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.transport.TransportConstants;

/**
 * Contains various utility methods for handling SOAP messages.
 *
 * @author Arjen Poutsma
 * @since 1.5.5
 */
public abstract class SoapUtils {

    private static final Pattern ACTION_PATTERN = Pattern.compile("action\\s*=\\s*([^;]+)");

    private SoapUtils() {
    }

    /** Escapes the given SOAP action to be surrounded by quotes. */
    public static String escapeAction(String soapAction) {
        if (!StringUtils.hasLength(soapAction)) {
            soapAction = "\"\"";
        }
        if (!soapAction.startsWith("\"")) {
            soapAction = "\"" + soapAction;
        }
        if (!soapAction.endsWith("\"")) {
            soapAction = soapAction + "\"";
        }
        return soapAction;
    }

    /**
     * Returns the value of the action parameter in the given SOAP 1.2 content type.
     *
     * @param contentType the SOAP 1.2 content type
     * @return the action
     */
    public static String extractActionFromContentType(String contentType) {
        if (contentType != null) {
            Matcher matcher = ACTION_PATTERN.matcher(contentType);
            if (matcher.find() && matcher.groupCount() == 1) {
                return matcher.group(1).trim();
            }
        }
        return TransportConstants.EMPTY_SOAP_ACTION;
    }

    /**
     * Replaces or adds the value of the action parameter in the given SOAP 1.2 content type.
     *
     * @param contentType the SOAP 1.2 content type
     * @param action      the action
     * @return the new content type
     */
    public static String setActionInContentType(String contentType, String action) {
        Assert.hasLength(contentType, "'contentType' must not be empty");
        if (StringUtils.hasText(action)) {
            Matcher matcher = ACTION_PATTERN.matcher(contentType);
            if (matcher.find() && matcher.groupCount() == 1) {
                StringBuffer buffer = new StringBuffer();
                matcher.appendReplacement(buffer, "action=" + action);
                matcher.appendTail(buffer);
                return buffer.toString();
            }
            else {
                return contentType + "; action=" + action;
            }
        }
        else {
            return contentType;
        }
    }


}
