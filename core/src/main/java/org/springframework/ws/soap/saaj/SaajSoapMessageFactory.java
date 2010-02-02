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

package org.springframework.ws.soap.saaj;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Iterator;
import java.util.StringTokenizer;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.soap.SoapMessageCreationException;
import org.springframework.ws.soap.SoapMessageFactory;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.saaj.support.SaajUtils;
import org.springframework.ws.transport.TransportConstants;
import org.springframework.ws.transport.TransportInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * SAAJ-specific implementation of the {@link org.springframework.ws.WebServiceMessageFactory WebServiceMessageFactory}.
 * Wraps a SAAJ {@link MessageFactory}. This factory will use SAAJ 1.3 when found, or fall back to SAAJ 1.2 or even
 * 1.1.
 * <p/>
 * A SAAJ {@link MessageFactory} can be injected to the {@link #SaajSoapMessageFactory(javax.xml.soap.MessageFactory)
 * constructor}, or by the {@link #setMessageFactory(javax.xml.soap.MessageFactory)} property. When a SAAJ message
 * factory is injected, the {@link #setSoapVersion(org.springframework.ws.soap.SoapVersion)} property is ignored.
 *
 * @author Arjen Poutsma
 * @see org.springframework.ws.soap.saaj.SaajSoapMessage
 * @since 1.0.0
 */
public class SaajSoapMessageFactory implements SoapMessageFactory, InitializingBean {

    private static final Log logger = LogFactory.getLog(SaajSoapMessageFactory.class);

    private MessageFactory messageFactory;

    private String messageFactoryProtocol;

    private boolean langAttributeOnSoap11FaultString = true;

    /** Default, empty constructor. */
    public SaajSoapMessageFactory() {
    }

    /** Constructor that takes a message factory as an argument. */
    public SaajSoapMessageFactory(MessageFactory messageFactory) {
        this.messageFactory = messageFactory;
    }

    /** Returns the SAAJ <code>MessageFactory</code> used. */
    public MessageFactory getMessageFactory() {
        return messageFactory;
    }

    /** Sets the SAAJ <code>MessageFactory</code>. */
    public void setMessageFactory(MessageFactory messageFactory) {
        this.messageFactory = messageFactory;
    }

    /**
     * Defines whether a {@code xml:lang} attribute should be set on SOAP 1.1 {@code <faultstring>} elements.
     * <p/>
     * The default is {@code true}, to comply with WS-I, but this flag can be set to {@code false} to the older W3C SOAP
     * 1.1 specification.
     *
     * @see <a href="http://www.ws-i.org/Profiles/BasicProfile-1.1.html#SOAP_Fault_Language">WS-I Basic Profile 1.1</a>
     */
    public void setlangAttributeOnSoap11FaultString(boolean langAttributeOnSoap11FaultString) {
        this.langAttributeOnSoap11FaultString = langAttributeOnSoap11FaultString;
    }

    public void setSoapVersion(SoapVersion version) {
        if (SaajUtils.getSaajVersion() >= SaajUtils.SAAJ_13) {
            if (SoapVersion.SOAP_11 == version) {
                messageFactoryProtocol = SOAPConstants.SOAP_1_1_PROTOCOL;
            }
            else if (SoapVersion.SOAP_12 == version) {
                messageFactoryProtocol = SOAPConstants.SOAP_1_2_PROTOCOL;
            }
            else {
                throw new IllegalArgumentException(
                        "Invalid version [" + version + "]. Expected the SOAP_11 or SOAP_12 constant");
            }
        }
        else if (SoapVersion.SOAP_11 != version) {
            throw new IllegalArgumentException("SAAJ 1.1 and 1.2 only support SOAP 1.1");
        }
    }

    public void afterPropertiesSet() throws Exception {
        if (messageFactory == null) {
            try {
                if (SaajUtils.getSaajVersion() >= SaajUtils.SAAJ_13) {
                    if (!StringUtils.hasLength(messageFactoryProtocol)) {
                        messageFactoryProtocol = SOAPConstants.SOAP_1_1_PROTOCOL;
                    }
                    if (logger.isInfoEnabled()) {
                        logger.info("Creating SAAJ 1.3 MessageFactory with " + messageFactoryProtocol);
                    }
                    messageFactory = MessageFactory.newInstance(messageFactoryProtocol);
                }
                else if (SaajUtils.getSaajVersion() == SaajUtils.SAAJ_12) {
                    logger.info("Creating SAAJ 1.2 MessageFactory");
                    messageFactory = MessageFactory.newInstance();
                }
                else if (SaajUtils.getSaajVersion() == SaajUtils.SAAJ_11) {
                    logger.info("Creating SAAJ 1.1 MessageFactory");
                    messageFactory = MessageFactory.newInstance();
                }
                else {
                    throw new IllegalStateException(
                            "SaajSoapMessageFactory requires SAAJ 1.1, which was not found on the classpath");
                }
            }
            catch (NoSuchMethodError ex) {
                throw new SoapMessageCreationException(
                        "Could not create SAAJ MessageFactory. Is the version of the SAAJ specification interfaces [" +
                                SaajUtils.getSaajVersionString() +
                                "] the same as the version supported by the application server?", ex);
            }
            catch (SOAPException ex) {
                throw new SoapMessageCreationException("Could not create SAAJ MessageFactory: " + ex.getMessage(), ex);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Using MessageFactory class [" + messageFactory.getClass().getName() + "]");
        }
    }

    public WebServiceMessage createWebServiceMessage() {
        try {
            return new SaajSoapMessage(messageFactory.createMessage(), langAttributeOnSoap11FaultString);
        }
        catch (SOAPException ex) {
            throw new SoapMessageCreationException("Could not create empty message: " + ex.getMessage(), ex);
        }
    }

    public WebServiceMessage createWebServiceMessage(InputStream inputStream) throws IOException {
        MimeHeaders mimeHeaders = parseMimeHeaders(inputStream);
        try {
            inputStream = checkForUtf8ByteOrderMark(inputStream);
            return new SaajSoapMessage(messageFactory.createMessage(mimeHeaders, inputStream));
        }
        catch (SOAPException ex) {
            // SAAJ 1.3 RI has a issue with handling multipart XOP content types which contain "startinfo" rather than
            // "start-info", so let's try and do something about it
            String contentType = StringUtils
                    .arrayToCommaDelimitedString(mimeHeaders.getHeader(TransportConstants.HEADER_CONTENT_TYPE));
            if (contentType.indexOf("startinfo") != -1) {
                contentType = contentType.replace("startinfo", "start-info");
                mimeHeaders.setHeader(TransportConstants.HEADER_CONTENT_TYPE, contentType);
                try {
                    return new SaajSoapMessage(messageFactory.createMessage(mimeHeaders, inputStream),
                            langAttributeOnSoap11FaultString);
                }
                catch (SOAPException e) {
                    // fall-through
                }
            }
            throw new SoapMessageCreationException("Could not create message from InputStream: " + ex.getMessage(), ex);
        }
    }

    private MimeHeaders parseMimeHeaders(InputStream inputStream) throws IOException {
        MimeHeaders mimeHeaders = new MimeHeaders();
        if (inputStream instanceof TransportInputStream) {
            TransportInputStream transportInputStream = (TransportInputStream) inputStream;
            for (Iterator headerNames = transportInputStream.getHeaderNames(); headerNames.hasNext();) {
                String headerName = (String) headerNames.next();
                for (Iterator headerValues = transportInputStream.getHeaders(headerName); headerValues.hasNext();) {
                    String headerValue = (String) headerValues.next();
                    StringTokenizer tokenizer = new StringTokenizer(headerValue, ",");
                    while (tokenizer.hasMoreTokens()) {
                        mimeHeaders.addHeader(headerName, tokenizer.nextToken().trim());
                    }
                }
            }
        }
        return mimeHeaders;
    }

    /**
     * Checks for the UTF-8 Byte Order Mark, and removes it if present. The SAAJ RI cannot cope with these BOMs.
     *
     * @see <a href="http://jira.springframework.org/browse/SWS-393">SWS-393</a>
     * @see <a href="http://unicode.org/faq/utf_bom.html#22">UTF-8 BOMs</a>
     */
    private InputStream checkForUtf8ByteOrderMark(InputStream inputStream) throws IOException {
        PushbackInputStream pushbackInputStream = new PushbackInputStream(new BufferedInputStream(inputStream), 3);
        byte[] bom = new byte[3];
        if (pushbackInputStream.read(bom) != -1) {
            // check for the UTF-8 BOM, and remove it if there. See SWS-393
            if (!(bom[0] == (byte) 0xEF && bom[1] == (byte) 0xBB && bom[2] == (byte) 0xBF)) {
                pushbackInputStream.unread(bom);
            }
        }
        return pushbackInputStream;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("SaajSoapMessageFactory[");
        builder.append(SaajUtils.getSaajVersionString());
        if (SaajUtils.getSaajVersion() >= SaajUtils.SAAJ_13) {
            builder.append(',');
            builder.append(messageFactoryProtocol);
        }
        builder.append(']');
        return builder.toString();
    }
}
