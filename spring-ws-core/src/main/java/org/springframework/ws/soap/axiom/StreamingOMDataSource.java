/*
 * Copyright 2005-2014 the original author or authors.
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

package org.springframework.ws.soap.axiom;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.Writer;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.util.StAXUtils;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.stream.StreamingPayload;

/**
 * Implementation of {@link OMDataSource} that wraps a {@link StreamingPayload}.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
class StreamingOMDataSource implements OMDataSource {

    private final StreamingPayload payload;

    StreamingOMDataSource(StreamingPayload payload) {
        Assert.notNull(payload, "'payload' must not be null");
        this.payload = payload;
    }

    @Override
    public void serialize(OutputStream output, OMOutputFormat format) throws XMLStreamException {
        XMLStreamWriter streamWriter;
        if (format != null && StringUtils.hasLength(format.getCharSetEncoding())) {
            streamWriter = StAXUtils.createXMLStreamWriter(output, format.getCharSetEncoding());
        }
        else {
            streamWriter = StAXUtils.createXMLStreamWriter(output);
        }
        serialize(streamWriter);
    }

    @Override
    public void serialize(Writer writer, OMOutputFormat format) throws XMLStreamException {
        XMLStreamWriter streamWriter = StAXUtils.createXMLStreamWriter(writer);
        serialize(streamWriter);
    }

    @Override
    public void serialize(XMLStreamWriter xmlWriter) throws XMLStreamException {
        payload.writeTo(xmlWriter);
        xmlWriter.flush();
    }

    @Override
    public XMLStreamReader getReader() throws XMLStreamException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        serialize(bos, null);

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        return StAXUtils.createXMLStreamReader(bis);
    }

}
