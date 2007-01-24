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

package org.springframework.ws.soap.server.endpoint.interceptor;

import java.util.Locale;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;

import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.interceptor.AbstractValidatingInterceptor;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.SoapFaultDetailElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.xml.namespace.QNameUtils;
import org.xml.sax.SAXParseException;

/**
 * Subclass of <code>AbstractValidatingInterceptor</code> that creates a SOAP Fault whenever the request message cannot
 * be validated. The contents of the SOAP Fault can be specified by setting the <code>addValidationErrorDetail</code>,
 * <code>faultStringOrReason</code>, or  <code>detailElementName</code> properties. Further customizing can be
 * accomplished by overriding <code>handleRequestValidationErrors</code>.
 *
 * @author Arjen Poutsma
 * @see #setAddValidationErrorDetail(boolean)
 * @see #setFaultStringOrReason(String)
 * @see #DEFAULT_FAULTSTRING_OR_REASON
 * @see #setDetailElementName(javax.xml.namespace.QName)
 * @see #DEFAULT_DETAIL_ELEMENT_NAME
 * @see #handleResponseValidationErrors(org.springframework.ws.context.MessageContext,org.xml.sax.SAXParseException[])
 */
public abstract class AbstractFaultCreatingValidatingInterceptor extends AbstractValidatingInterceptor {

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

    private boolean addValidationErrorDetail = true;

    private QName detailElementName = DEFAULT_DETAIL_ELEMENT_NAME;

    private String faultStringOrReason = DEFAULT_FAULTSTRING_OR_REASON;

    private Locale faultStringOrReasonLocale = Locale.ENGLISH;

    /**
     * Returns whether a SOAP Fault detail element should be created when a validation error occurs. This detail element
     * will contain the exact validation errors. It is only added when the underlying message is a
     * <code>SoapMessage</code>. Defaults to <code>true</code>.
     *
     * @see org.springframework.ws.soap.SoapFault#addFaultDetail()
     */
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

    protected boolean handleRequestValidationErrors(MessageContext messageContext, SAXParseException[] errors)
            throws TransformerException {
        for (int i = 0; i < errors.length; i++) {
            logger.warn("XML validation error on request: " + errors[i].getMessage());
        }
        if (messageContext.getResponse() instanceof SoapMessage) {
            SoapMessage response = (SoapMessage) messageContext.getResponse();
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
        return false;
    }
}
