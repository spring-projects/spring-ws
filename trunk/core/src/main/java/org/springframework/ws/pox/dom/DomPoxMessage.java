/*
 * Copyright 2006 the original author or authors.
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

package org.springframework.ws.pox.dom;

import java.io.IOException;
import java.io.OutputStream;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.springframework.ws.pox.PoxMessage;
import org.springframework.ws.transport.TransportOutputStream;
import org.w3c.dom.Document;

/**
 * Implementation of the <code>PoxMessage</code> interface that is based on a DOM Document.
 *
 * @author Arjen Poutsma
 * @see Document
 */
public class DomPoxMessage implements PoxMessage {

    private static final String CONTENT_TYPE = "text/xml";

    private final Document document;

    private Transformer transformer;

    /**
     * Constructs a new instance of the <code>DomPoxMessage</code> with the given document.
     *
     * @param document the document to base the message on
     */
    public DomPoxMessage(Document document, Transformer transformer) {
        this.document = document;
        this.transformer = transformer;
    }

    /**
     * Returns the document underlying this message.
     */
    public Document getDocument() {
        return document;
    }

    public Result getPayloadResult() {
        return new DOMResult(document);
    }

    public Source getPayloadSource() {
        return new DOMSource(document);
    }

    public void writeTo(OutputStream outputStream) throws IOException {
        try {
            if (outputStream instanceof TransportOutputStream) {
                TransportOutputStream transportOutputStream = (TransportOutputStream) outputStream;
                transportOutputStream.addHeader("Content-Type", CONTENT_TYPE);
            }
            transformer.transform(getPayloadSource(), new StreamResult(outputStream));
        }
        catch (TransformerException ex) {
            throw new DomPoxMessageException("Could write document: " + ex.getMessage(), ex);
        }
    }
}
