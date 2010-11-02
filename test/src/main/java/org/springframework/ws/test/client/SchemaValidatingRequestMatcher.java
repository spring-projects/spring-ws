/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.test.client;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.xml.validation.XmlValidator;

import org.xml.sax.SAXParseException;

/**
 * Uses the {@link XmlValidator} to validate request payload.
 *
 * @author Lukas Krecan
 * @since 2.0
 */
class SchemaValidatingRequestMatcher implements RequestMatcher {

    private final XmlValidator xmlValidator;

    public SchemaValidatingRequestMatcher(XmlValidator xmlValidator) {
        Assert.notNull(xmlValidator, "XmlValidator has to be set");
        this.xmlValidator = xmlValidator;
    }

    public void match(URI uri, WebServiceMessage request) throws IOException, AssertionError {
        SAXParseException[] exceptions = xmlValidator.validate(request.getPayloadSource());
        if (!ObjectUtils.isEmpty(exceptions)) {
            throw new AssertionError("XML is not valid: " + Arrays.toString(exceptions));
        }
    }
}
