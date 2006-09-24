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
import org.springframework.util.Assert;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.context.MessageContextFactory;
import org.springframework.ws.soap.SoapMessageCreationException;
import org.springframework.ws.transport.TransportContext;
import org.springframework.ws.transport.TransportRequest;

/**
 * Axiom-specific implementation of the <code>MessageContextFactory</code> interface. Creates a
 * <code>AxiomSoapMessageContext</code>.
 * <p/>
 * To increase reading performance on the the SOAP request created by this message context factory, you can set the
 * <code>payloadCaching</code> property to <code>false</code> (default is <code>true</code>). This this will read the
 * contents of the body directly from the <code>TransportRequest</code>. However, <strong>when this setting is enabled,
 * the payload can only be read once</strong>. This means that any endpoint mappings or interceptors which are based on
 * the message payload (such as the <code>PayloadRootQNameEndpointMapping</code>, the
 * <code>PayloadValidatingInterceptor</code>, or the <code>PayloadLoggingInterceptor</code>) cannot be used. Instead,
 * use an endpoint mapping that does not consume the payload (i.e. the <code>SoapActionEndpointMapping</code>).
 * <p/>
 * Mostly derived from <code>org.apache.axis2.transport.http.HTTPTransportUtils</code> and
 * <code>org.apache.axis2.transport.TransportUtils</code>, which we cannot use since they are not part of the Axiom
 * distribution.
 *
 * @author Arjen Poutsma
 * @see AxiomSoapMessageContext
 * @see #setPayloadCaching(boolean)
 */
public class AxiomSoapMessageContextFactory implements MessageContextFactory, InitializingBean {

    private static final Log logger = LogFactory.getLog(AxiomSoapMessageContextFactory.class);

    private static final String CHAR_SET_ENCODING = "charset";

    private static final String DEFAULT_CHAR_SET_ENCODING = "UTF-8";

    private static final String CONTENT_TYPE_HEADER = "Content-Type";

    private static final String MULTI_PART_RELATED_CONTENT_TYPE = "multipart/related";

    private boolean payloadCaching = true;

    private SOAP11Factory soap11Factory;

    private SOAP12Factory soap12Factory;

    /**
     * Indicates whether the SOAP Body payload should be cached or not. Default is <code>true</code>. Setting this to
     * <code>false</code> will increase performance, but also result in the fact that the message payload can only be
     * read once.
     */
    public void setPayloadCaching(boolean payloadCaching) {
        this.payloadCaching = payloadCaching;
    }

    private XMLInputFactory inputFactory;

    public void afterPropertiesSet() throws Exception {
        inputFactory = XMLInputFactory.newInstance();
        soap11Factory = new SOAP11Factory();
        soap12Factory = new SOAP12Factory();
        if (logger.isInfoEnabled()) {
            logger.info(payloadCaching ? "Enabled payload caching" : "Disabled payload caching");
        }
    }

    public MessageContext createContext(TransportContext transportContext) throws IOException {
        TransportRequest transportRequest = transportContext.getTransportRequest();
        Iterator iterator = transportRequest.getHeaders(CONTENT_TYPE_HEADER);
        Assert.isTrue(iterator.hasNext(), "No " + CONTENT_TYPE_HEADER + " header present of TransportRequest");
        String contentType = (String) iterator.next();
        Assert.hasLength(contentType, "No " + CONTENT_TYPE_HEADER + " header present of TransportRequest");
        InputStream inputStream = transportRequest.getInputStream();
        try {
            AxiomSoapMessage requestMessage;
            if (contentType.indexOf(MULTI_PART_RELATED_CONTENT_TYPE) == -1) {
                XMLStreamReader reader =
                        inputFactory.createXMLStreamReader(inputStream, getCharSetEncoding(contentType));
                SOAPFactory soapFactory = getSoapFactory(contentType);
                StAXSOAPModelBuilder builder =
                        new StAXSOAPModelBuilder(reader, soapFactory, soapFactory.getSoapVersionURI());
                requestMessage = createAxiomSoapMessage(builder, null);
            }
            else {
                requestMessage = createMultiPartAxiomSoapMessage(inputStream, contentType);
            }
            return new AxiomSoapMessageContext(requestMessage, transportRequest);

        }
        catch (XMLStreamException ex) {
            throw new SoapMessageCreationException("Could not create message: " + ex.getMessage(), ex);
        }
        catch (OMException ex) {
            throw new SoapMessageCreationException("Could not create message: " + ex.getMessage(), ex);
        }
    }

    private AxiomSoapMessage createMultiPartAxiomSoapMessage(InputStream inputStream, String contentType)
            throws XMLStreamException {
        Attachments attachments = new Attachments(inputStream, contentType);
        if (!(attachments.getAttachmentSpecType().equals(MTOMConstants.SWA_TYPE) ||
                attachments.getAttachmentSpecType().equals(MTOMConstants.MTOM_TYPE))) {
            throw new SoapMessageCreationException(
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

    /**
     * Creates a new <code>AxiomSoapMessage</code> based on the given parameters.
     *
     * @param modelBuilder the builder used to optain the Axiom SOAPMessage
     * @param attachments  the attachments, can be <code>null</code>
     * @return the created message
     */
    private AxiomSoapMessage createAxiomSoapMessage(StAXSOAPModelBuilder modelBuilder, Attachments attachments) {
        SOAPMessage soapMessage = modelBuilder.getSoapMessage();
        return new AxiomSoapMessage(soapMessage, attachments, payloadCaching);
    }

    private SOAPFactory getSoapFactory(String contentType) {
        if (contentType.indexOf(SOAP11Constants.SOAP_11_CONTENT_TYPE) != -1) {
            return soap11Factory;
        }
        else if (contentType.indexOf(SOAP12Constants.SOAP_12_CONTENT_TYPE) != -1) {
            return soap12Factory;
        }
        else {
            throw new SoapMessageCreationException("Unknown content type '" + contentType + "'");
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
