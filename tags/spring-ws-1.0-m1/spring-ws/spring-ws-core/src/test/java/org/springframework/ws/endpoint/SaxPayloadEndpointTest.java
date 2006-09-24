/*
 * Copyright 2005 the original author or authors.
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

package org.springframework.ws.endpoint;

import java.io.StringReader;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.xml.sax.ContentHandler;

public class SaxPayloadEndpointTest extends TestCase {

    private MySaxPayloadEndpoint endpoint;

    private MockControl contentHandlerControl;

    private ContentHandler contentHandlerMock;

    protected void setUp() throws Exception {
        contentHandlerControl = MockControl.createStrictControl(ContentHandler.class);
        contentHandlerMock = (ContentHandler) contentHandlerControl.getMock();
        endpoint = new MySaxPayloadEndpoint(contentHandlerMock);
    }

    public void testInvoke() throws Exception {
        Source requestSource = new StreamSource(new StringReader("<request/>"));
        Source responseSource = new StreamSource(new StringReader("<response/>"));
        endpoint.setResponse(responseSource);

        contentHandlerMock.setDocumentLocator(null);
        contentHandlerControl.setMatcher(MockControl.ALWAYS_MATCHER);
        contentHandlerMock.startDocument();
        contentHandlerMock.startElement("", "request", "request", null);
        contentHandlerControl.setMatcher(MockControl.ALWAYS_MATCHER);
        contentHandlerMock.endElement("", "request", "request");
        contentHandlerMock.endDocument();
        contentHandlerControl.replay();
        Source result = endpoint.invoke(requestSource);
        contentHandlerControl.verify();
        assertEquals("Invalid response", responseSource, result);
    }

    private class MySaxPayloadEndpoint extends AbstractSaxPayloadEndpoint {

        private ContentHandler contentHandler;

        private Source response;

        public MySaxPayloadEndpoint(ContentHandler contentHandler) {
            this.contentHandler = contentHandler;
        }

        public void setResponse(Source response) {
            this.response = response;
        }

        protected Source getResponse(ContentHandler contentHandler) {
            assertEquals("Invalid contentHandler", this.contentHandler, contentHandler);
            return response;
        }

        protected ContentHandler createContentHandler() {
            return contentHandler;
        }

    }

}
