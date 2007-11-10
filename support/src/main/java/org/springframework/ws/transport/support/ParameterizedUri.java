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

package org.springframework.ws.transport.support;

import java.util.Map;
import java.util.StringTokenizer;

import org.springframework.core.CollectionFactory;
import org.springframework.util.Assert;

/** @author Arjen Poutsma */
public class ParameterizedUri {

    private final String uri;

    private final String scheme;

    // keys are string parameter names; values are string parameter values
    private final Map parameters = CollectionFactory.createLinkedCaseInsensitiveMapIfPossible(5);

    private final String destination;

    public ParameterizedUri(String uri) {
        Assert.hasLength(uri, "'uri' must not be empty");
        this.uri = uri;
        int scIdx = uri.indexOf(':');
        Assert.isTrue(scIdx != -1, uri + " does contain scheme");
        scheme = uri.substring(0, scIdx);
        Assert.isTrue(uri.length() > scheme.length(), uri + " does not have a destination");
        int paramStart = uri.indexOf('?');
        if (paramStart == -1) {
            destination = uri.substring(scIdx + 1);
        }
        else {
            destination = uri.substring(scIdx + 1, paramStart);
            parseParameters(uri.substring(paramStart + 1));
        }
    }

    private void parseParameters(String parametersString) {
        StringTokenizer params = new StringTokenizer(parametersString, "&");
        while (params.hasMoreTokens()) {
            String param = params.nextToken();
            int paramSep = param.indexOf('=');
            if (paramSep == -1) {
                throw new IllegalArgumentException(param + " is not a valid parameter: it has no '='");
            }
            String paramName = param.substring(0, paramSep);
            String paramValue = param.substring(paramSep + 1);
            parameters.put(paramName, paramValue);
        }
    }

    /** Returns the destination of the uri. */
    protected String getDestination() {
        return destination;
    }

    public String toString() {
        return uri;
    }

    protected String getParameter(String paramName) {
        return (String) parameters.get(paramName);
    }

    protected boolean hasParameter(String paramName) {
        return parameters.containsKey(paramName);
    }
}
