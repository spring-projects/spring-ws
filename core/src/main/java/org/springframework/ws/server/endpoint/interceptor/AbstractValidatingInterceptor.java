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

package org.springframework.ws.server.endpoint.interceptor;

import java.io.IOException;
import java.util.Locale;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.SoapFaultDetailElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.xml.namespace.QNameUtils;
import org.springframework.xml.transform.TransformerObjectSupport;
import org.springframework.xml.validation.XmlValidator;
import org.springframework.xml.validation.XmlValidatorFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Abstract base class for <code>EndpointInterceptor</code> implementations that validate part of the message using a
 * schema. The exact message part is determined by the <code>getValidationRequestSource</code> and
 * <code>getValidationResponseSource</code> template methods.
 * <p/>
 * By default, only the request message is validated, but this behaviour can be changed using the
 * <code>validateRequest</code> and <code>validateResponse</code> properties. Responses that contains SOAP faults are
 * not validated.
 *
 * @author Arjen Poutsma
 * @see #getValidationRequestSource(org.springframework.ws.WebServiceMessage)
 * @see #getValidationResponseSource(org.springframework.ws.WebServiceMessage)
 */
public abstract class AbstractValidatingInterceptor extends TransformerObjectSupport
        implements EndpointInterceptor, InitializingBean {

    /**
     * Default SOAP Fault Detail name used when a validation errors occur on the request.
     *
     * @see #setDetailElementName(javax.xml.namespace.QName)
     */
    public static final QName DEFAULT_DETAIL_ELEMENT_NAME =
            QNameUtils.createQName("http://springframework.org/spring-ws", "ValidationError", "spring-ws");

    /**
     * Default SOAP Fault string used when a validation errors occur on the request.
     *
     * @see #setFaultStringOrReason(String)
     */
    public static final String DEFAULT_FAULTSTRING_OR_REASON = "Validation error";

    protected final Log logger = LogFactory.getLog(getClass());

    private boolean addValidationErrorDetail = true;

    private QName detailElementName = DEFAULT_DETAIL_ELEMENT_NAME;

    private String faultStringOrReason = DEFAULT_FAULTSTRING_OR_REASON;

    private Locale faultStringOrReasonLocale = Locale.ENGLISH;

    private String schemaLanguage = XmlValidatorFactory.SCHEMA_W3C_XML;

    private Resource[] schemas;

    private boolean validateRequest = true;

    private boolean validateResponse = false;

    private XmlValidator validator;

    public boolean getAddValidationErrorDetail() {
        return addValidationErrorDetail;
    }

    /**
     * Indicates whether a SOAP Fault detail element should be created when a validation error occurs. This detail
     * element will contain the exact validation errors. It is only added when the underlying message is a
     * <code>SoapMessage</code>. Defaults to <code>true</code>.
     *
     * @see org.springframework.ws.soap.SoapFault#addFaultDetail()
     */
    public void setAddValidationErrorDetail(boolean addValidationErrorDetail) {
        this.addValidationErrorDetail = addValidationErrorDetail;
    }

    /**
     * Returns the fault detail element name when validation errors occur on the request.
     */
    public QName getDetailElementName() {
        return detailElementName;
    }

    /**
     * Sets the fault detail element name when validation errors occur on the request. Defaults to
     * <code>DEFAULT_DETAIL_ELEMENT_NAME</code>.
     *
     * @see #DEFAULT_DETAIL_ELEMENT_NAME
     */
    public void setDetailElementName(QName detailElementName) {
        this.detailElementName = detailElementName;
    }

    /**
     * Sets the SOAP <code>faultstring</code> or <code>Reason</code> used when validation errors occur on the request.
     */
    public String getFaultStringOrReason() {
        return faultStringOrReason;
    }

    /**
     * Sets the SOAP <code>faultstring</code> or <code>Reason</code> used when validation errors occur on the request.
     * It is only added when the underlying message is a <code>SoapMessage</code>. Defaults to
     * <code>DEFAULT_FAULTSTRING_OR_REASON</code>.
     *
     * @see #DEFAULT_FAULTSTRING_OR_REASON
     */
    public void setFaultStringOrReason(String faultStringOrReason) {
        this.faultStringOrReason = faultStringOrReason;
    }

    /**
     * Returns the SOAP fault reason locale used when validation errors occur on the request.
     */
    public Locale getFaultStringOrReasonLocale() {
        return faultStringOrReasonLocale;
    }

    /**
     * Sets the SOAP fault reason locale used when validation errors occur on the request.  It is only added when the
     * underlying message is a <code>SoapMessage</code>. Defaults to English.
     *
     * @see java.util.Locale#ENGLISH
     */
    public void setFaultStringOrReasonLocale(Locale faultStringOrReasonLocale) {
        this.faultStringOrReasonLocale = faultStringOrReasonLocale;
    }

    public String getSchemaLanguage() {
        return schemaLanguage;
    }

    /**
     * Sets the schema language. Default is the W3C XML Schema: <code>http://www.w3.org/2001/XMLSchema"</code>.
     *
     * @see org.springframework.xml.validation.XmlValidatorFactory#SCHEMA_W3C_XML
     * @see org.springframework.xml.validation.XmlValidatorFactory#SCHEMA_RELAX_NG
     */
    public void setSchemaLanguage(String schemaLanguage) {
        this.schemaLanguage = schemaLanguage;
    }

    /**
     * Returns the schema resources to use for validation.
     */
    public Resource[] getSchemas() {
        return schemas;
    }

    /**
     * Sets the schema resources to use for validation.  Setting either this property or <code>schema</code> is
     * required.
     */
    public void setSchemas(Resource[] schemas) {
        Assert.notEmpty(schemas, "schemas must not be empty or null");
        for (int i = 0; i < schemas.length; i++) {
            Assert.notNull(schemas[i], "schema must not be null");
            Assert.isTrue(schemas[i].exists(), "schema \"" + schemas[i] + "\" does not exit");
        }
        this.schemas = schemas;
    }

    /**
     * Sets the schema resource to use for validation.  Setting either this property or <code>schemas</code> is
     * required.
     */
    public void setSchema(Resource schema) {
        setSchemas(new Resource[]{schema});
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
     * @return <code>true</code> if the message is valid; <code>false</code> otherwise
     * @see #setValidateRequest(boolean)
     */
    public boolean handleRequest(MessageContext messageContext, Object endpoint)
            throws IOException, SAXException, TransformerException {
        if (validateRequest) {
            Source requestSource = getValidationRequestSource(messageContext.getRequest());
            if (requestSource != null) {
                SAXParseException[] errors = validator.validate(requestSource);
                if (!ObjectUtils.isEmpty(errors)) {
                    for (int i = 0; i < errors.length; i++) {
                        logger.warn("XML validation error on request: " + errors[i].getMessage());
                    }
                    if (messageContext.getResponse() instanceof SoapMessage) {
                        createRequestValidationFault((SoapMessage) messageContext.getResponse(), errors);
                    }
                    return false;
                }
                else if (logger.isDebugEnabled()) {
                    logger.debug("Request message validated");
                }
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
     * @return <code>true</code> if the response is valid; <code>false</code> otherwise
     * @see #setValidateResponse(boolean)
     */
    public boolean handleResponse(MessageContext messageContext, Object endpoint) throws IOException, SAXException {
        if (validateResponse) {
            Source responseSource = getValidationResponseSource(messageContext.getResponse());
            if (responseSource != null) {
                SAXParseException[] errors = validator.validate(responseSource);
                if (!ObjectUtils.isEmpty(errors)) {
                    for (int i = 0; i < errors.length; i++) {
                        logger.error("XML validation error on response: " + errors[i].getMessage());
                    }
                    return false;
                }
                else if (logger.isDebugEnabled()) {
                    logger.debug("Response message validated");
                }
            }
        }
        return true;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notEmpty(schemas, "setting either the schema or schemas property is required");
        Assert.hasLength(schemaLanguage, "schemaLanguage is required");
        for (int i = 0; i < schemas.length; i++) {
            Assert.isTrue(schemas[i].exists(), "schema [" + schemas[i] + "] does not exist");
        }
        if (logger.isInfoEnabled()) {
            logger.info("Validating using " + StringUtils.arrayToCommaDelimitedString(schemas));
        }
        validator = XmlValidatorFactory.createValidator(schemas, schemaLanguage);
    }

    /**
     * Creates a response soap message containing a <code>SoapFault</code> that descibes the validation errors.
     */
    protected void createRequestValidationFault(SoapMessage response, SAXParseException[] errors)
            throws TransformerException {
        SoapBody body = response.getSoapBody();
        SoapFault fault = body.addClientOrSenderFault(getFaultStringOrReason(), getFaultStringOrReasonLocale());
        if (getAddValidationErrorDetail()) {
            SoapFaultDetail detail = fault.addFaultDetail();
            for (int i = 0; i < errors.length; i++) {
                SoapFaultDetailElement detailElement = detail.addFaultDetailElement(getDetailElementName());
                detailElement.addText(errors[i].getMessage());
            }
        }
    }

    /**
     * Abstract template method that returns the part of the request message that is to be validated.
     *
     * @param request the request message
     * @return the part of the message that is to validated, or <code>null</code> not to validate anything
     */
    protected abstract Source getValidationRequestSource(WebServiceMessage request);

    /**
     * Abstract template method that returns the part of the response message that is to be validated.
     *
     * @param response the response message
     * @return the part of the message that is to validated, or <code>null</code> not to validate anything
     */
    protected abstract Source getValidationResponseSource(WebServiceMessage response);
}
