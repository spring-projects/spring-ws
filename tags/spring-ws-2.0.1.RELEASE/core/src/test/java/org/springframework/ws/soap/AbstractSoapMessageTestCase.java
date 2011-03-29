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

package org.springframework.ws.soap;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.springframework.ws.mime.AbstractMimeMessageTestCase;
import org.springframework.ws.mime.MimeMessage;
import org.springframework.ws.stream.StreamingPayload;
import org.springframework.ws.stream.StreamingWebServiceMessage;
import org.springframework.ws.transport.MockTransportOutputStream;
import org.springframework.ws.transport.TransportConstants;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.validation.XmlValidator;
import org.springframework.xml.validation.XmlValidatorFactory;

import org.junit.Test;
import org.xml.sax.SAXParseException;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.*;

public abstract class AbstractSoapMessageTestCase extends AbstractMimeMessageTestCase {

    protected SoapMessage soapMessage;

    @Override
    protected MimeMessage createMimeMessage() throws Exception {
        soapMessage = createSoapMessage();
        return soapMessage;
    }

    protected abstract SoapMessage createSoapMessage() throws Exception;

    @Test
    public void testValidate() throws Exception {
        XmlValidator validator =
                XmlValidatorFactory.createValidator(getSoapSchemas(), XmlValidatorFactory.SCHEMA_W3C_XML);
        SAXParseException[] errors = validator.validate(soapMessage.getEnvelope().getSource());
        if (errors.length > 0) {
            fail(StringUtils.arrayToCommaDelimitedString(errors));
        }
    }

    @Test
    public void testSoapAction() throws Exception {
        assertEquals("Invalid default SOAP Action", "\"\"", soapMessage.getSoapAction());
        soapMessage.setSoapAction("SoapAction");
        assertEquals("Invalid SOAP Action", "\"SoapAction\"", soapMessage.getSoapAction());
    }

    @Test
    public void testCharsetAttribute() throws Exception {
        MockTransportOutputStream outputStream = new MockTransportOutputStream(new ByteArrayOutputStream());
        soapMessage.writeTo(outputStream);
        Map<String, String> headers = outputStream.getHeaders();
        String contentType = headers.get(TransportConstants.HEADER_CONTENT_TYPE);
        if (contentType != null) {
            Pattern charsetPattern = Pattern.compile("charset\\s*=\\s*([^;]+)");
            Matcher matcher = charsetPattern.matcher(contentType);
            if (matcher.find() && matcher.groupCount() == 1) {
                String charset = matcher.group(1).trim();
                assertTrue("Invalid charset", charset.indexOf('"') < 0);
            }
        }
    }

    @Test
    public void testSetStreamingPayload() throws Exception {
        if (!(soapMessage instanceof StreamingWebServiceMessage)) {
            return;
        }
        StreamingWebServiceMessage streamingMessage = (StreamingWebServiceMessage) soapMessage;

        final QName name = new QName("http://springframework.org", "root", "prefix");
        streamingMessage.setStreamingPayload(new StreamingPayload() {
            public QName getName() {
                return name;
            }

            public void writeTo(XMLStreamWriter streamWriter) throws XMLStreamException {
                streamWriter.writeStartElement(name.getPrefix(), name.getLocalPart(), name.getNamespaceURI());
                streamWriter.writeNamespace("prefix", name.getNamespaceURI());
                streamWriter.writeStartElement(name.getNamespaceURI(), "child");
                streamWriter.writeCharacters("Foo");
                streamWriter.writeEndElement();
                streamWriter.writeEndElement();
            }
        });

        StringResult result = new StringResult();
        transformer.transform(streamingMessage.getPayloadSource(), result);

        String expected = "<root xmlns='http://springframework.org'><child>Foo</child></root>";
        assertXMLEqual(expected, result.toString());

        soapMessage.writeTo(new ByteArrayOutputStream());
    }

    protected abstract Resource[] getSoapSchemas();

    @Test
    public abstract void testGetVersion() throws Exception;

    @Test
    public abstract void testWriteToTransportOutputStream() throws Exception;

    @Test
    public abstract void testWriteToTransportResponseAttachment() throws Exception;

    @Test
    public abstract void testToDocument() throws Exception;

    @Test
    public abstract void testSetLiveDocument() throws Exception;

    @Test
    public abstract void testSetOtherDocument() throws Exception;


}
