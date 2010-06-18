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

import java.io.IOException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.springframework.core.io.Resource;
import org.springframework.ws.WebServiceMessage;
import org.springframework.xml.sax.SaxUtils;
import org.springframework.xml.transform.StringResult;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import static org.custommonkey.xmlunit.XMLAssert.fail;

/**
 * Abstract base class that matches payloads.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
abstract class PayloadMatcher extends DiffMatcher {

    @Override
    protected final Diff createDiff(WebServiceMessage request) throws Exception {
        Source payload = request.getPayloadSource();
        if (payload == null) {
            fail("Request message does not contain payload");
        }
        return createDiff(payload);
    }

    protected abstract Diff createDiff(Source payload) throws Exception;

    public static PayloadMatcher createStringPayloadMatcher(final String control) {
        return new PayloadMatcher() {
            @Override
            protected Diff createDiff(Source payload) throws TransformerException, IOException, SAXException {
                StringResult result = new StringResult();
                transform(payload, result);
                return new Diff(control, result.toString());
            }
        };
    }

    public static PayloadMatcher createResourcePayloadMatcher(final Resource control) {
        return new PayloadMatcher() {
            @Override
            protected Diff createDiff(Source payload) throws IOException, SAXException, TransformerException {
                InputSource controlInputSource = SaxUtils.createInputSource(control);
                Document controlDocument = XMLUnit.buildDocument(XMLUnit.newControlParser(), controlInputSource);
                DOMSource controlSource = new DOMSource(controlDocument);

                DOMResult result = new DOMResult();
                transform(payload, result);
                DOMSource resultSource = new DOMSource(result.getNode());

                return new Diff(controlSource, resultSource);
            }
        };
    }

    public static PayloadMatcher createSourcePayloadMatcher(final Source control) {
        return new PayloadMatcher() {
            @Override
            protected Diff createDiff(Source payload) throws Exception {
                if (control instanceof DOMSource && payload instanceof DOMSource) {
                    return new Diff((DOMSource) control, (DOMSource) payload);
                }
                DOMResult controlResult = new DOMResult();
                transform(control, controlResult);
                DOMSource controlSource = new DOMSource(controlResult.getNode());

                DOMResult payloadResult = new DOMResult();
                transform(payload, payloadResult);
                DOMSource payloadSource = new DOMSource(payloadResult.getNode());

                return new Diff(controlSource, payloadSource);
            }
        };
    }
}
