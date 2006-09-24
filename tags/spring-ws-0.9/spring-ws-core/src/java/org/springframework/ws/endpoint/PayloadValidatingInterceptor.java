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

package org.springframework.ws.endpoint;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapEndpointInterceptor;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.context.SoapMessageContext;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Interceptor that validates the contents of <code>WebServiceMessage</code>s using a schema. Allows for both W3C XML
 * and RELAX NG schemas.
 * <p/>
 * When the payload is invalid, this interceptor stops processing of the interceptor chain. Additionally, if the message
 * is a SOAP request message, a SOAP Fault is created as reply. Invalid SOAP responses do not result in a fault.
 * <p/>
 * The schema to validate against is set with the <code>schema</code> property. By default, only the request message is
 * validated, but this behaviour can be changed using the <code>validateRequest</code> and <code>validateResponse</code>
 * properties. Responses that contains faults are not validated.
 *
 * @author Arjen Poutsma
 * @see #setSchema
 * @see #setValidateRequest(boolean)
 * @see #setValidateResponse(boolean)
 */
public class PayloadValidatingInterceptor implements SoapEndpointInterceptor, InitializingBean {

    private static final Log logger = LogFactory.getLog(PayloadValidatingInterceptor.class);

    private String schemaLanguage = XMLConstants.W3C_XML_SCHEMA_NS_URI;

    private Resource schemaResource;

    private boolean validateRequest = true;

    private boolean validateResponse = false;

    private Schema schema;

    /**
     * Sets the schema resource to use for validation.
     */
    public void setSchema(Resource schema) {
        this.schemaResource = schema;
    }

    /**
     * Sets the schema language. Default is the W3C XML Schema: <code>http://www.w3.org/2001/XMLSchema"</code>.
     *
     * @see XMLConstants#W3C_XML_SCHEMA_NS_URI
     * @see XMLConstants#RELAXNG_NS_URI
     */
    public void setSchemaLanguage(String schemaLanguage) {
        this.schemaLanguage = schemaLanguage;
    }

    /**
     * Indicates whether the request should be validated against the schema. Default is <code>true</code>.
     */
    public void setValidateRequest(boolean validateRequest) {
        this.validateRequest = validateRequest;
    }

    /**
     * Indicates whether the response should be validated against the schema. Default is <code>false</code>.
     */
    public void setValidateResponse(boolean validateResponse) {
        this.validateResponse = validateResponse;
    }

    /**
     * Validates the request message in the given message context. Validation only occurs if
     * <code>validateRequest</code> is set to <code>true</code>, which is the default.
     * <p/>
     * Returns <code>true</code> if the request is valid, or <code>false</code> if it isn't. Additionally, when the
     * <code>messageContext</code> is a <code>SoapMessageContext</code>, a SOAP Fault is added as response.
     *
     * @param messageContext the message context
     * @param endpoint
     * @return <code>true</code> if the message is valid; <code>false</code> otherwise
     * @see #setValidateRequest(boolean)
     */
    public boolean handleRequest(MessageContext messageContext, Object endpoint) throws IOException, SAXException {
        if (validateRequest) {
            List errors = validate(messageContext.getRequest());
            if (!errors.isEmpty()) {
                for (Iterator iterator = errors.iterator(); iterator.hasNext();) {
                    SAXParseException ex = (SAXParseException) iterator.next();
                    logger.warn("XML validation error on request: " + ex.getMessage());
                }
                if (messageContext instanceof SoapMessageContext) {
                    SoapMessage response = ((SoapMessageContext) messageContext).createSoapResponse();
                    response.addFault(new QName("Client"), "Validation error", null);
                }
                return false;
            }
            else if (logger.isDebugEnabled()) {
                logger.debug("Request message validated");
            }
        }
        return true;
    }

    /**
     * Validates the response message in the given message context. Validation only occurs if
     * <code>validateResponse</code> is set to <code>true</code>, which is <strong>not</strong> the default.
     * <p/>
     * Returns <code>true</code> if the request is valid, or <code>false</code> if it isn't.
     *
     * @param messageContext the message context.
     * @param endpoint
     * @return <code>true</code> if the response is valid; <code>false</code> otherwise
     * @see #setValidateResponse(boolean)
     */
    public boolean handleResponse(MessageContext messageContext, Object endpoint) throws IOException, SAXException {
        if (validateResponse) {
            List errors = validate(messageContext.getResponse());
            if (!errors.isEmpty()) {
                for (Iterator iterator = errors.iterator(); iterator.hasNext();) {
                    SAXParseException ex = (SAXParseException) iterator.next();
                    logger.error("XML validation error on response: " + ex.getMessage());
                }
                return false;
            }
            else if (logger.isDebugEnabled()) {
                logger.debug("Response message validated");
            }
        }
        return true;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(schemaResource, "schema is required");
        SchemaFactory schemaFactory = SchemaFactory.newInstance(schemaLanguage);
        InputStream inputStream = schemaResource.getInputStream();
        try {
            Source schemaSource = new StreamSource(inputStream);
            schema = schemaFactory.newSchema(schemaSource);
        }
        finally {
            inputStream.close();
        }
    }

    /**
     * Returns <code>true</code>, i.e. SOAP Faults are not validated.
     */
    public boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
        return true;
    }

    /**
     * Returns <code>false</code>, i.e. SOAP Headers are not understood.
     */
    public boolean understands(Element header) {
        return false;
    }

    private List validate(WebServiceMessage message) throws IOException, SAXException {
        ValidationErrorHandler errorHandler = new ValidationErrorHandler();
        Validator validator = schema.newValidator();
        validator.setErrorHandler(errorHandler);
        validator.validate(message.getPayloadSource());
        return errorHandler.getErrors();
    }

    /**
     * <code>ErrorHandler</code> implementation that logs warnings to the <code>PayloadValidatingInterceptor</code>
     * logger, and stores errors and fatal errors in a list.
     */
    private static class ValidationErrorHandler implements ErrorHandler {

        private List errors = new ArrayList();

        public List getErrors() {
            return errors;
        }

        public void warning(SAXParseException ex) throws SAXException {
            logger.debug("Ignored XML validation warning: " + ex.getMessage());
        }

        public void error(SAXParseException ex) throws SAXException {
            errors.add(ex);
        }

        public void fatalError(SAXParseException ex) throws SAXException {
            errors.add(ex);
        }
    }
}
