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

import java.io.InputStream;
import java.io.Reader;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * Strategy interface for dealing with various {@link Source} implementations. By using this interface, in combination
 * with {@link TraxUtils#handleSource(Source, SourceHandler)} , implementations are freed from <code>instanceof</code>
 * checks typically seen in TrAX code.
 *
 * @author Arjen Poutsma
 * @see TraxUtils#handleSource(Source, SourceHandler)
 * @since 1.5.0
 */
public interface SourceHandler {

    /** Handles a {@link DOMSource} with a {@link Node}. */
    void domSource(Node node);

    /** Handles a {@link SAXSource} with an {@link XMLReader} and an {@link InputSource}. */
    void saxSource(XMLReader xmlReader, InputSource inputSource);

    /** Handles a StAX Source with an {@link XMLEventReader}, either in a {@link StaxSource} or a {@link StAXSource}. */
    void staxSource(XMLEventReader eventReader);

    /** Handles a StAX Source with an {@link XMLStreamReader}, either in a {@link StaxSource} or a {@link StAXSource}. */
    void staxSource(XMLStreamReader streamReader);

    /** Handles a {@link StreamSource} with an {@link InputStream}. */
    void streamSource(InputStream inputStream);

    /** Handles a {@link StreamSource} with an {@link Reader}. */
    void streamSource(Reader reader);

}
