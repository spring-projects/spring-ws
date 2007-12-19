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

package org.springframework.xml.transform;

import java.io.OutputStream;
import java.io.Writer;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stax.StAXResult;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;

/**
 * Strategy interface for dealing with various {@link Result} implementations. By using this interface, in combination
 * with {@link TraxUtils#handleResult(Result, ResultHandler)} , implementations are freed from <code>instanceof</code>
 * checks typically seen in TrAX code.
 *
 * @author Arjen Poutsma
 * @see TraxUtils#handleResult(Result, ResultHandler)
 * @since 1.5.0
 */
public interface ResultHandler {

    /**
     * Handles a {@link DOMSource} with a {@link Node}.
     * <p/>
     * In practice, node is typically a {@link Document}, a {@link DocumentFragment}, or a {@link Element}. In other
     * words, a node that accepts children.
     */
    void domResult(Node node);

    /** Handles a {@link SAXResult} with a {@link ContentHandler} and possibly a {@link LexicalHandler}. */
    void saxResult(ContentHandler contentHandler, LexicalHandler lexicalHandler);

    /** Handles a StAX Result with an {@link XMLEventWriter}, either in a {@link StaxResult} or a {@link StAXResult}. */
    void staxResult(XMLEventWriter eventWriter);

    /** Handles a StAX Result with an {@link XMLStreamWriter}, either in a {@link StaxResult} or a {@link StAXResult}. */
    void staxResult(XMLStreamWriter streamWriter);

    /** Handles a {@link StreamResult} with an {@link OutputStream}. */
    void streamResult(OutputStream outputStream);

    /** Handles a {@link StreamResult} with an {@link Writer}. */
    void streamResult(Writer writer);
}
