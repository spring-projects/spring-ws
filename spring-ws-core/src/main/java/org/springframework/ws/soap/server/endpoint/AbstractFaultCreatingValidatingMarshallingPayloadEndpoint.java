/*
 * Copyright 2005-2022 the original author or authors.
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

package org.springframework.ws.soap.server.endpoint;

import java.util.Locale;

import javax.xml.namespace.QName;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.SoapFaultDetailElement;
import org.springframework.ws.soap.SoapMessage;

/**
 * Extension of the {@link org.springframework.ws.server.endpoint.AbstractValidatingMarshallingPayloadEndpoint} which
 * validates the request payload with {@link Validator}(s), and creates a SOAP Fault whenever the request message cannot
 * be validated. The desired validators can be set using properties, and <strong>must</strong>
 * {@link Validator#supports(Class) support} the request object.
 * <p>
 * The contents of the SOAP Fault can be specified by setting the {@link #setAddValidationErrorDetail(boolean)
 * addValidationErrorDetail}, {@link #setFaultStringOrReason(String) faultStringOrReason}, or
 * {@link #setDetailElementName(QName) detailElementName} properties.
 *
 * @author Arjen Poutsma
 * @since 1.0.2
 * @deprecated as of Spring Web Services 2.0, in favor of annotated endpoints
 */
@Deprecated
public abstract class AbstractFaultCreatingValidatingMarshallingPayloadEndpoint extends
		org.springframework.ws.server.endpoint.AbstractValidatingMarshallingPayloadEndpoint implements MessageSourceAware {

	/**
	 * Default SOAP Fault Detail name used when a global validation error occur on the request.
	 *
	 * @see #setDetailElementName(javax.xml.namespace.QName)
	 */
	public static final QName DEFAULT_DETAIL_ELEMENT_NAME = new QName("http://springframework.org/spring-ws",
			"ValidationError", "spring-ws");

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

	private MessageSource messageSource;

	/**
	 * Returns whether a SOAP Fault detail element should be created when a validation error occurs. This detail element
	 * will contain the exact validation errors. It is only added when the underlying message is a {@code SoapMessage}.
	 * Defaults to {@code true}.
	 *
	 * @see org.springframework.ws.soap.SoapFault#addFaultDetail()
	 */
	public boolean getAddValidationErrorDetail() {
		return addValidationErrorDetail;
	}

	/**
	 * Indicates whether a SOAP Fault detail element should be created when a validation error occurs. This detail element
	 * will contain the exact validation errors. It is only added when the underlying message is a {@code SoapMessage}.
	 * Defaults to {@code true}.
	 *
	 * @see org.springframework.ws.soap.SoapFault#addFaultDetail()
	 */
	public void setAddValidationErrorDetail(boolean addValidationErrorDetail) {
		this.addValidationErrorDetail = addValidationErrorDetail;
	}

	/** Returns the fault detail element name when validation errors occur on the request. */
	public QName getDetailElementName() {
		return detailElementName;
	}

	/**
	 * Sets the fault detail element name when validation errors occur on the request. Defaults to
	 * {@code DEFAULT_DETAIL_ELEMENT_NAME}.
	 *
	 * @see #DEFAULT_DETAIL_ELEMENT_NAME
	 */
	public void setDetailElementName(QName detailElementName) {
		this.detailElementName = detailElementName;
	}

	/** Sets the SOAP {@code faultstring} or {@code Reason} used when validation errors occur on the request. */
	public String getFaultStringOrReason() {
		return faultStringOrReason;
	}

	/**
	 * Sets the SOAP {@code faultstring} or {@code Reason} used when validation errors occur on the request. It is only
	 * added when the underlying message is a {@code SoapMessage}. Defaults to {@code DEFAULT_FAULTSTRING_OR_REASON}.
	 *
	 * @see #DEFAULT_FAULTSTRING_OR_REASON
	 */
	public void setFaultStringOrReason(String faultStringOrReason) {
		this.faultStringOrReason = faultStringOrReason;
	}

	/** Returns the locale for SOAP fault reason and validation message resolution. */
	public Locale getFaultLocale() {
		return faultStringOrReasonLocale;
	}

	/**
	 * Sets the locale for SOAP fault reason and validation messages. It is only added when the underlying message is a
	 * {@code SoapMessage}. Defaults to English.
	 *
	 * @see java.util.Locale#ENGLISH
	 */
	public void setFaultStringOrReasonLocale(Locale faultStringOrReasonLocale) {
		this.faultStringOrReasonLocale = faultStringOrReasonLocale;
	}

	@Override
	public final void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	/**
	 * This implementation logs all errors, returns {@code false}, and creates a
	 * {@link SoapBody#addClientOrSenderFault(String,Locale) client or sender} {@link SoapFault}, adding a
	 * {@link SoapFaultDetail} with all errors if the {@code addValidationErrorDetail} property is {@code true}.
	 *
	 * @param messageContext the message context
	 * @param errors the validation errors
	 * @return {@code true} to continue processing the request, {@code false} (the default) otherwise
	 * @see Errors#getAllErrors()
	 */
	@Override
	protected final boolean onValidationErrors(MessageContext messageContext, Object requestObject, Errors errors) {
		for (ObjectError objectError : errors.getAllErrors()) {
			String msg = messageSource.getMessage(objectError, getFaultLocale());
			logger.warn("Validation error on request object[" + requestObject + "]: " + msg);
		}
		if (messageContext.getResponse() instanceof SoapMessage) {
			SoapMessage response = (SoapMessage) messageContext.getResponse();
			SoapBody body = response.getSoapBody();
			SoapFault fault = body.addClientOrSenderFault(getFaultStringOrReason(), getFaultLocale());
			if (getAddValidationErrorDetail()) {
				SoapFaultDetail detail = fault.addFaultDetail();
				for (ObjectError objectError : errors.getAllErrors()) {
					String msg = messageSource.getMessage(objectError, getFaultLocale());
					SoapFaultDetailElement detailElement = detail.addFaultDetailElement(getDetailElementName());
					detailElement.addText(msg);
				}
			}
		}
		return false;
	}
}
