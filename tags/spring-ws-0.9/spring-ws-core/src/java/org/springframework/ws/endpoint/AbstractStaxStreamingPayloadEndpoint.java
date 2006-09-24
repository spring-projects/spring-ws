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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Abstract base class for endpoints that handle the message payload with StAX. Allows subclasses to read the request
 * with a <code>XMLStreamReader</code>, and to create a response using a <code>XMLStreamWriter</code>.
 *
 * @author Arjen Poutsma
 * @see #invokeInternal(javax.xml.stream.XMLStreamReader, javax.xml.stream.XMLStreamWriter)
 * @see XMLStreamReader
 * @see XMLStreamWriter
 */
public abstract class AbstractStaxStreamingPayloadEndpoint implements PayloadEndpoint, InitializingBean {

    protected final Log logger = LogFactory.getLog(getClass());

    private XMLInputFactory inputFactory;

    private XMLOutputFactory outputFactory;

    public final void afterPropertiesSet() throws Exception {
        inputFactory = XMLInputFactory.newInstance();
        outputFactory = XMLOutputFactory.newInstance();
        onAfterPropertiesSet();
    }

    public final Source invoke(Source request) throws Exception {
        XMLStreamReader streamReader = createStreamReader(request);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        XMLStreamWriter streamWriter = outputFactory.createXMLStreamWriter(os);
        invokeInternal(streamReader, streamWriter);
        streamWriter.flush();
        byte[] buffer = os.toByteArray();
        return (buffer.length != 0) ? new StreamSource(new ByteArrayInputStream(buffer)) : null;
    }

    private XMLStreamReader createStreamReader(Source source) throws XMLStreamException, TransformerException {
        XMLStreamReader streamReader = inputFactory.createXMLStreamReader(source);
        if (streamReader == null) {
            // XMLInputFactory.createXMLStreamReader(Source) is not implemented in all implementations, so we have a
            // workaround
            logger.info("XMLInputFactory.createXMLStreamReader(Source) not implemented, using workaround");
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            transformer.transform(source, new StreamResult(os));
            ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
            streamReader = inputFactory.createXMLStreamReader(is);
        }
        return streamReader;
    }

    /**
     * Template method which can be used for initalization.
     */
    protected void onAfterPropertiesSet() throws Exception {
    }

    /**
     * Template method. Subclasses must implement this. Offers the request payload as a <code>XMLStreamReader</code>,
     * and a <code>XMLStreamWriter</code> to write the response payload to.
     *
     * @param streamReader the reader to read the payload from
     * @param streamWriter the writer to write the payload to
     */
    protected abstract void invokeInternal(XMLStreamReader streamReader, XMLStreamWriter streamWriter) throws Exception;
}
