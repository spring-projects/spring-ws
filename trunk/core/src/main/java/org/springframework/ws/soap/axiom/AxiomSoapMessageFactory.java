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

package org.springframework.ws.soap.axiom;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.impl.MTOMConstants;
import org.apache.axiom.om.impl.mtom.MTOMStAXSOAPModelBuilder;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPMessage;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axiom.soap.impl.llom.soap11.SOAP11Factory;
import org.apache.axiom.soap.impl.llom.soap12.SOAP12Factory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.transport.TransportInputStream;

/**
 * Axiom-specific implementation of the {@link org.springframework.ws.WebServiceMessageFactory WebServiceMessageFactory}
 * interface. Creates {@link org.springframework.ws.soap.axiom.AxiomSoapMessage AxiomSoapMessages}.
 * <p/>
 * To increase reading performance on the the SOAP request created by this message context factory, you can set the
 * <code>payloadCaching</code> property to <code>false</code> (default is <code>true</code>). This this will read the
 * contents of the body directly from the stream. However, <strong>when this setting is enabled, the payload can only be
 * read once</strong>. This means that any endpoint mappings or interceptors which are based on the message payload
 * (such as the <code>PayloadRootQNameEndpointMapping</code>, the <code>PayloadValidatingInterceptor</code>, or the
 * <code>PayloadLoggingInterceptor</code>) cannot be used. Instead, use an endpoint mapping that does not consume the
 * payload (i.e. the <code>SoapActionEndpointMapping</code>).
 * <p/>
 * Mostly derived from <code>org.apache.axis2.transport.http.HTTPTransportUtils</code> and
 * <code>org.apache.axis2.transport.TransportUtils</code>, which we cannot use since they are not part of the Axiom
 * distribution.
 *
 * @author Arjen Poutsma
 * @see AxiomSoapMessage
 * @see #setPayloadCaching(boolean)
 */
public class AxiomSoapMessageFactory implements WebServiceMessageFactory, InitializingBean {

    private static final String CHAR_SET_ENCODING = "charset";

    private static final String CONTENT_TYPE_HEADER = "Content-Type";

    private static final String DEFAULT_CHAR_SET_ENCODING = "UTF-8";

    private static final String MULTI_PART_RELATED_CONTENT_TYPE = "multipart/related";

    private static final Log logger = LogFactory.getLog(AxiomSoapMessageFactory.class);

    private XMLInputFactory inputFactory;

    private boolean payloadCaching = true;

    private SOAP11Factory soap11Factory = new SOAP11Factory();

    private SOAP12Factory soap12Factory = new SOAP12Factory();

    /**
     * Indicates whether the SOAP Body payload should be cached or not. Default is <code>true</code>. Setting this to
     * <code>false</code> will increase performance, but also result in the fact that the message payload can only be
     * read once.
     */
    public void setPayloadCaching(boolean payloadCaching) {
        this.payloadCaching = payloadCaching;
    }

    public AxiomSoapMessageFactory() {
        inputFactory = XMLInputFactory.newInstance();
    }

    public void afterPropertiesSet() throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info(payloadCaching ? "Enabled payload caching" : "Disabled payload caching");
        }
    }

    public WebServiceMessage createWebServiceMessage() {
        return new AxiomSoapMessage(soap11Factory);
    }

    public WebServiceMessage createWebServiceMessage(InputStream inputStream) throws IOException {
        String contentType = null;
        if (inputStream instanceof TransportInputStream) {
            TransportInputStream transportInputStream = (TransportInputStream) inputStream;
            Iterator iterator = transportInputStream.getHeaders(CONTENT_TYPE_HEADER);
            if (iterator.hasNext()) {
                contentType = (String) iterator.next();
            }
        }
        if (!StringUtils.hasLength(contentType)) {
            // fall back to SOAP 1.1 as a default
            contentType = SOAP11Constants.SOAP_11_CONTENT_TYPE;
        }
        try {
            if (isMultiPartRelated(contentType)) {
                return createMultiPartAxiomSoapMessage(inputStream, contentType);
            }
            else {
                return createAxiomSoapMessage(inputStream, contentType);
            }
        }
        catch (XMLStreamException ex) {
            throw new AxiomSoapMessageCreationException("Could not parse request: " + ex.getMessage(), ex);
        }
        catch (OMException ex) {
            throw new AxiomSoapMessageCreationException("Could not create message: " + ex.getMessage(), ex);
        }
    }

    private boolean isMultiPartRelated(String contentType) {
        return contentType.indexOf(MULTI_PART_RELATED_CONTENT_TYPE) != -1;
    }

    /**
     * Creates an AxiomSoapMessage without attachments.
     */
    private WebServiceMessage createAxiomSoapMessage(InputStream inputStream, String contentType)
            throws XMLStreamException {
        XMLStreamReader reader = inputFactory.createXMLStreamReader(inputStream, getCharSetEncoding(contentType));
        SOAPFactory soapFactory = getSoapFactory(contentType);
        StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(reader, soapFactory, soapFactory.getSoapVersionURI());
        SOAPMessage soapMessage = builder.getSoapMessage();
        return new AxiomSoapMessage(soapMessage, payloadCaching);
    }

    /**
     * Creates an AxiomSoapMessage with attachments.
     */
    private AxiomSoapMessage createMultiPartAxiomSoapMessage(InputStream inputStream, String contentType)
            throws XMLStreamException {
        Attachments attachments = new Attachments(inputStream, contentType);
        if (!(attachments.getAttachmentSpecType().equals(MTOMConstants.SWA_TYPE) ||
                attachments.getAttachmentSpecType().equals(MTOMConstants.MTOM_TYPE))) {
            throw new AxiomSoapMessageCreationException(
                    "Unknown attachment type: [" + attachments.getAttachmentSpecType() + "]");
        }
        XMLStreamReader reader = inputFactory.createXMLStreamReader(attachments.getSOAPPartInputStream(),
                getCharSetEncoding(attachments.getSOAPPartContentType()));
        SOAPFactory soapFactory = getSoapFactory(attachments.getSOAPPartContentType());
        StAXSOAPModelBuilder builder = null;
        if (attachments.getAttachmentSpecType().equals(MTOMConstants.SWA_TYPE)) {
            builder = new StAXSOAPModelBuilder(reader, soapFactory, soapFactory.getSoapVersionURI());
        }
        else if (attachments.getAttachmentSpecType().equals(MTOMConstants.MTOM_TYPE)) {
            builder = new MTOMStAXSOAPModelBuilder(reader, attachments, soapFactory.getSoapVersionURI());
        }
        return new AxiomSoapMessage(builder.getSoapMessage(), attachments, payloadCaching);
    }

    private SOAPFactory getSoapFactory(String contentType) {
        if (contentType.indexOf(SOAP11Constants.SOAP_11_CONTENT_TYPE) != -1) {
            return soap11Factory;
        }
        else if (contentType.indexOf(SOAP12Constants.SOAP_12_CONTENT_TYPE) != -1) {
            return soap12Factory;
        }
        else {
            throw new AxiomSoapMessageCreationException("Unknown content type '" + contentType + "'");
        }
    }

    /**
     * Returns the character set from the given content type. Mostly copied
     *
     * @return the character set encoding
     */
    protected String getCharSetEncoding(String contentType) {
        int index = contentType.indexOf(CHAR_SET_ENCODING);
        if (index == -1) {
            return DEFAULT_CHAR_SET_ENCODING;
        }
        int idx = contentType.indexOf("=", index);

        int indexOfSemiColon = contentType.indexOf(";", idx);
        String value;

        if (indexOfSemiColon > 0) {
            value = contentType.substring(idx + 1, indexOfSemiColon);
        }
        else {
            value = contentType.substring(idx + 1, contentType.length()).trim();
        }
        if (value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') {
            return value.substring(1, value.length() - 1);
        }
        if ("null".equalsIgnoreCase(value)) {
            return DEFAULT_CHAR_SET_ENCODING;
        }
        else {
            return value.trim();
        }
    }

}
