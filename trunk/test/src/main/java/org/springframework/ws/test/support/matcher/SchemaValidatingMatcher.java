/*
 * Copyright 2005-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.test.support.matcher;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.xml.validation.XmlValidator;
import org.springframework.xml.validation.XmlValidatorFactory;

import org.xml.sax.SAXParseException;

import static org.springframework.ws.test.support.AssertionErrors.fail;

/**
 * Uses the {@link XmlValidator} to validate request payload.
 *
 * @author Lukas Krecan
 * @author Arjen Poutsma
 * @since 2.0
 */
public class SchemaValidatingMatcher implements WebServiceMessageMatcher {

    private final XmlValidator xmlValidator;

    /**
     * Creates a {@code SchemaValidatingMatcher} based on the given schema resource(s).
     *
     * @param schema         the schema
     * @param furtherSchemas further schemas, if necessary
     * @throws IOException in case of I/O errors
     */
    public SchemaValidatingMatcher(Resource schema, Resource... furtherSchemas) throws IOException {
        Assert.notNull(schema, "'schema' must not be null");
        Resource[] joinedSchemas = new Resource[furtherSchemas.length + 1];
        joinedSchemas[0] = schema;
        System.arraycopy(furtherSchemas, 0, joinedSchemas, 1, furtherSchemas.length);
        xmlValidator = XmlValidatorFactory.createValidator(joinedSchemas, XmlValidatorFactory.SCHEMA_W3C_XML);

    }

    public void match(WebServiceMessage message) throws IOException, AssertionError {
        SAXParseException[] exceptions = xmlValidator.validate(message.getPayloadSource());
        if (!ObjectUtils.isEmpty(exceptions)) {
            fail("XML is not valid: " + Arrays.toString(exceptions), "Payload", message.getPayloadSource());
        }
    }
}
