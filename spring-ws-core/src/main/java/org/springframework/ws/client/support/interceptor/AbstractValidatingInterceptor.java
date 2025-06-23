/*
 * Copyright 2005-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.client.support.interceptor;

import java.io.IOException;

import javax.xml.transform.Source;

import org.jspecify.annotations.Nullable;
import org.xml.sax.SAXParseException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.WebServiceIOException;
import org.springframework.ws.context.MessageContext;
import org.springframework.xml.transform.TransformerObjectSupport;
import org.springframework.xml.validation.XmlValidator;
import org.springframework.xml.validation.XmlValidatorFactory;
import org.springframework.xml.xsd.XsdSchema;
import org.springframework.xml.xsd.XsdSchemaCollection;

/**
 * Abstract base class for {@link ClientInterceptor} implementations that validate part of
 * the message using a schema. The exact message part is determined by the
 * {@link #getValidationRequestSource(WebServiceMessage)} and
 * {@link #getValidationResponseSource(WebServiceMessage)} template methods.
 * <p>
 * By default, only the request message is validated, but this behaviour can be changed
 * using the {@code validateRequest} and {@code validateResponse} properties.
 *
 * @author Arjen Poutsma
 * @since 1.5.4
 * @see #getValidationRequestSource(WebServiceMessage)
 * @see #getValidationResponseSource(WebServiceMessage)
 */
public abstract class AbstractValidatingInterceptor extends TransformerObjectSupport
		implements ClientInterceptor, InitializingBean {

	private String schemaLanguage = XmlValidatorFactory.SCHEMA_W3C_XML;

	@SuppressWarnings("NullAway.Init")
	private Resource[] schemas;

	private boolean validateRequest = true;

	private boolean validateResponse = false;

	@SuppressWarnings("NullAway.Init")
	private XmlValidator validator;

	public String getSchemaLanguage() {
		return this.schemaLanguage;
	}

	/**
	 * Sets the schema language. Default is the W3C XML Schema:
	 * {@code http://www.w3.org/2001/XMLSchema"}.
	 * @see XmlValidatorFactory#SCHEMA_W3C_XML
	 * @see XmlValidatorFactory#SCHEMA_RELAX_NG
	 */
	public void setSchemaLanguage(String schemaLanguage) {
		this.schemaLanguage = schemaLanguage;
	}

	/**
	 * Returns the schema resources to use for validation.
	 */
	public Resource[] getSchemas() {
		return this.schemas;
	}

	/**
	 * Sets the schema resource to use for validation. Setting this property,
	 * {@link #setXsdSchemaCollection(XsdSchemaCollection) xsdSchemaCollection},
	 * {@link #setSchemas(Resource[]) schemas}, or {@link #setXsdSchema(XsdSchema)
	 * xsdSchema} is required.
	 */
	public void setSchema(Resource schema) {
		setSchemas(schema);
	}

	/**
	 * Sets the schema resources to use for validation. Setting this property,
	 * {@link #setXsdSchemaCollection(XsdSchemaCollection) xsdSchemaCollection},
	 * {@link #setSchema(Resource) schema}, or {@link #setXsdSchema(XsdSchema) xsdSchema}
	 * is required.
	 */
	public void setSchemas(Resource... schemas) {
		Assert.notEmpty(schemas, "schemas must not be empty or null");
		for (Resource schema : schemas) {
			Assert.notNull(schema, "schema must not be null");
			Assert.isTrue(schema.exists(), "schema \"" + schema + "\" does not exit");
		}
		this.schemas = schemas;
	}

	/**
	 * Sets the {@link XsdSchema} to use for validation. Setting this property,
	 * {@link #setXsdSchemaCollection(XsdSchemaCollection) xsdSchemaCollection},
	 * {@link #setSchema(Resource) schema}, or {@link #setSchemas(Resource[]) schemas} is
	 * required.
	 * @param schema the xsd schema to use
	 * @throws java.io.IOException in case of I/O errors
	 */
	public void setXsdSchema(XsdSchema schema) throws IOException {
		this.validator = schema.createValidator();
	}

	/**
	 * Sets the {@link XsdSchemaCollection} to use for validation. Setting this property,
	 * {@link #setXsdSchema(XsdSchema) xsdSchema}, {@link #setSchema(Resource) schema}, or
	 * {@link #setSchemas(Resource[]) schemas} is required.
	 * @param schemaCollection the xsd schema collection to use
	 * @throws java.io.IOException in case of I/O errors
	 */
	public void setXsdSchemaCollection(XsdSchemaCollection schemaCollection) throws IOException {
		this.validator = schemaCollection.createValidator();
	}

	/**
	 * Indicates whether the request should be validated against the schema. Default is
	 * {@code true}.
	 */
	public void setValidateRequest(boolean validateRequest) {
		this.validateRequest = validateRequest;
	}

	/**
	 * Indicates whether the response should be validated against the schema. Default is
	 * {@code false}.
	 */
	public void setValidateResponse(boolean validateResponse) {
		this.validateResponse = validateResponse;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (this.validator == null && !ObjectUtils.isEmpty(this.schemas)) {
			Assert.hasLength(this.schemaLanguage, "schemaLanguage is required");
			for (Resource schema : this.schemas) {
				Assert.isTrue(schema.exists(), "schema [" + schema + "] does not exist");
			}
			if (this.logger.isInfoEnabled()) {
				this.logger.info("Validating using " + StringUtils.arrayToCommaDelimitedString(this.schemas));
			}
			this.validator = XmlValidatorFactory.createValidator(this.schemas, this.schemaLanguage);
		}
		Assert.notNull(this.validator,
				"Setting 'schema', 'schemas', 'xsdSchema', or 'xsdSchemaCollection' is required");
	}

	/**
	 * Validates the request message in the given message context. Validation only occurs
	 * if {@link #setValidateRequest(boolean) validateRequest} is set to {@code true},
	 * which is the default.
	 * <p>
	 * Returns {@code true} if the request is valid, or {@code false} if it isn't.
	 * @param messageContext the message context
	 * @return {@code true} if the message is valid; {@code false} otherwise
	 * @see #setValidateRequest(boolean)
	 */
	@Override
	public boolean handleRequest(MessageContext messageContext) throws WebServiceClientException {
		if (this.validateRequest) {
			Source requestSource = getValidationRequestSource(messageContext.getRequest());
			if (requestSource != null) {
				SAXParseException[] errors;
				try {
					errors = this.validator.validate(requestSource);
				}
				catch (IOException ex) {
					throw new WebServiceIOException("Could not validate request: " + ex.getMessage(), ex);
				}
				if (!ObjectUtils.isEmpty(errors)) {
					return handleRequestValidationErrors(messageContext, errors);
				}
				else if (this.logger.isDebugEnabled()) {
					this.logger.debug("Request message validated");
				}
			}
		}
		return true;
	}

	/**
	 * Template method that is called when the request message contains validation errors.
	 * <p>
	 * Default implementation logs all errors, and throws a
	 * {@link WebServiceValidationException}. Subclasses can override this method to
	 * customize this behavior.
	 * @param messageContext the message context
	 * @param errors the validation errors
	 * @return {@code true} to continue processing the request, {@code false} otherwise
	 */
	protected boolean handleRequestValidationErrors(MessageContext messageContext, SAXParseException[] errors) {
		for (SAXParseException error : errors) {
			this.logger.error("XML validation error on request: " + error.getMessage());
		}
		throw new WebServiceValidationException(errors);
	}

	/**
	 * Validates the response message in the given message context. Validation only occurs
	 * if {@link #setValidateResponse(boolean) validateResponse} is set to {@code true},
	 * which is <strong>not</strong> the default.
	 * <p>
	 * Returns {@code true} if the request is valid, or {@code false} if it isn't.
	 * @param messageContext the message context.
	 * @return {@code true} if the response is valid; {@code false} otherwise
	 * @see #setValidateResponse(boolean)
	 */
	@Override
	public boolean handleResponse(MessageContext messageContext) throws WebServiceClientException {
		if (this.validateResponse) {
			Source responseSource = getValidationResponseSource(messageContext.getResponse());
			if (responseSource != null) {
				SAXParseException[] errors;
				try {
					errors = this.validator.validate(responseSource);
				}
				catch (IOException ex) {
					throw new WebServiceIOException("Could not validate response: " + ex.getMessage(), ex);
				}
				if (!ObjectUtils.isEmpty(errors)) {
					return handleResponseValidationErrors(messageContext, errors);
				}
				else if (this.logger.isDebugEnabled()) {
					this.logger.debug("Response message validated");
				}
			}
		}
		return true;
	}

	/**
	 * Template method that is called when the response message contains validation
	 * errors.
	 * <p>
	 * Default implementation logs all errors, and returns {@code false}, i.e. do not cot
	 * continue to process the respone interceptor chain.
	 * @param messageContext the message context
	 * @param errors the validation errors
	 * @return {@code true} to continue the reponse interceptor chain, {@code false} (the
	 * default) otherwise
	 */
	protected boolean handleResponseValidationErrors(MessageContext messageContext, SAXParseException[] errors)
			throws WebServiceValidationException {
		for (SAXParseException error : errors) {
			this.logger.warn("XML validation error on response: " + error.getMessage());
		}
		return false;
	}

	/** Does nothing by default. Faults are not validated. */
	@Override
	public boolean handleFault(MessageContext messageContext) throws WebServiceClientException {
		return true;
	}

	/** Does nothing by default. */
	@Override
	public void afterCompletion(MessageContext messageContext, @Nullable Exception ex)
			throws WebServiceClientException {
	}

	/**
	 * Abstract template method that returns the part of the request message that is to be
	 * validated.
	 * @param request the request message
	 * @return the part of the message that is to validated, or {@code null} not to
	 * validate anything
	 */
	protected abstract @Nullable Source getValidationRequestSource(WebServiceMessage request);

	/**
	 * Abstract template method that returns the part of the response message that is to
	 * be validated.
	 * @param response the response message
	 * @return the part of the message that is to validated, or {@code null} not to
	 * validate anything
	 */
	protected abstract @Nullable Source getValidationResponseSource(WebServiceMessage response);

}
