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

package org.springframework.ws.mock.client;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;

import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;

import org.custommonkey.xmlunit.Diff;
import org.w3c.dom.Document;

import static org.springframework.ws.mock.client.Assert.fail;

/**
 * Matches {@link Source} payloads.
 *
 * @author Arjen Poutsma
 * @author Lukas Krecan
 * @since 2.0
 */
class PayloadDiffMatcher extends DiffMatcher {

    private final Source expected;

    PayloadDiffMatcher(Source expected) {
        Assert.notNull(expected, "'expected' must not be null");
        this.expected = expected;
    }

    @Override
    protected final Diff createDiff(WebServiceMessage request) throws Exception {
        Source payload = request.getPayloadSource();
        if (payload == null) {
            fail("Request message does not contain payload");
        }
        return createDiff(payload);
    }

    protected Diff createDiff(Source payload) throws TransformerException {
        Document expectedDocument = createDocumentFromSource(expected);
        Document actualDocument = createDocumentFromSource(payload);
        return new Diff(expectedDocument, actualDocument);
    }

    private Document createDocumentFromSource(Source source) throws TransformerException {
        DOMResult result = new DOMResult();
        transform(source, result);
        return (Document) result.getNode();
    }
}