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

package org.springframework.ws.soap.axiom;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Locale;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.impl.MTOMConstants;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPMessage;
import org.apache.axiom.soap.SOAPModelBuilder;
import org.apache.axiom.soap.SOAPVersion;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.server.endpoint.interceptor.PayloadLoggingInterceptor;
import org.springframework.ws.server.endpoint.mapping.PayloadRootAnnotationMethodEndpointMapping;
import org.springframework.ws.soap.SoapMessageFactory;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.server.endpoint.interceptor.PayloadValidatingInterceptor;
import org.springframework.ws.soap.server.endpoint.mapping.SoapActionAnnotationMethodEndpointMapping;
import org.springframework.ws.soap.support.SoapUtils;
import org.springframework.ws.transport.TransportConstants;
import org.springframework.ws.transport.TransportInputStream;
import org.springframework.xml.XMLInputFactoryUtils;

/**
 * Axiom-specific implementation of the
 * {@link org.springframework.ws.WebServiceMessageFactory WebServiceMessageFactory}
 * interface. Creates {@link org.springframework.ws.soap.axiom.AxiomSoapMessage
 * AxiomSoapMessages}.
 * <p>
 * To increase reading performance on the SOAP request created by this message factory,
 * you can set the {@link #setPayloadCaching(boolean) payloadCaching} property to
 * {@code false} (default is {@code true}). This this will read the contents of the body
 * directly from the stream. However, <strong>when this setting is enabled, the payload
 * can only be read once</strong>. This means that any endpoint mappings or interceptors
 * which are based on the message payload (such as the
 * {@link PayloadRootAnnotationMethodEndpointMapping}, the
 * {@link PayloadValidatingInterceptor}, or the {@link PayloadLoggingInterceptor}) cannot
 * be used. Instead, use an endpoint mapping that does not consume the payload (i.e. the
 * {@link SoapActionAnnotationMethodEndpointMapping}).
 * <p>
 * Additionally, this message factory can cache large attachments to disk by setting the
 * {@link #setAttachmentCaching(boolean) attachmentCaching} property to {@code true}
 * (default is {@code false}). Optionally, the location where attachments are stored can
 * be defined via the {@link #setAttachmentCacheDir(File) attachmentCacheDir} property
 * (defaults to the system temp file path).
 * <p>
 * Mostly derived from {@code org.apache.axis2.transport.http.HTTPTransportUtils} and
 * {@code org.apache.axis2.transport.TransportUtils}, which we cannot use since they are
 * not part of the Axiom distribution.
 *
 * @author Arjen Poutsma
 * @author Andreas Veithen
 * @since 1.0.0
 * @see AxiomSoapMessage
 * @see #setPayloadCaching(boolean)
 */
public class AxiomSoapMessageFactory implements SoapMessageFactory, InitializingBean {

	private static final String CHARSET_PARAMETER = "charset";

	private static final String DEFAULT_CHARSET_ENCODING = "UTF-8";

	private static final String MULTI_PART_RELATED_CONTENT_TYPE = "multipart/related";

	private static final Log logger = LogFactory.getLog(AxiomSoapMessageFactory.class);

	private @Nullable XMLInputFactory inputFactory;

	private boolean payloadCaching = true;

	private boolean attachmentCaching = false;

	@SuppressWarnings("NullAway.Init")
	private File attachmentCacheDir;

	private int attachmentCacheThreshold = 4096;

	// use SOAP 1.1 by default
	private SOAPFactory soapFactory = OMAbstractFactory.getSOAP11Factory();

	private boolean langAttributeOnSoap11FaultString = true;

	private boolean replacingEntityReferences = false;

	private boolean supportingExternalEntities = false;

	/**
	 * Indicates whether the SOAP Body payload should be cached or not. Default is
	 * {@code true}.
	 * <p>
	 * Setting this to {@code false} will increase performance, but also result in the
	 * fact that the message payload can only be read once.
	 */
	public void setPayloadCaching(boolean payloadCaching) {
		this.payloadCaching = payloadCaching;
	}

	/**
	 * Indicates whether SOAP attachments should be cached or not. Default is
	 * {@code false}.
	 * <p>
	 * Setting this to {@code true} will cause Axiom to store larger attachments on disk,
	 * rather than in memory. This decreases memory consumption, but decreases
	 * performance.
	 */
	public void setAttachmentCaching(boolean attachmentCaching) {
		this.attachmentCaching = attachmentCaching;
	}

	/**
	 * Sets the directory where SOAP attachments will be stored. Only used when
	 * {@link #setAttachmentCaching(boolean) attachmentCaching} is set to {@code true}.
	 * <p>
	 * The parameter should be an existing, writable directory. This property defaults to
	 * the temporary directory of the operating system (i.e. the value of the
	 * {@code java.io.tmpdir} system property).
	 */
	public void setAttachmentCacheDir(File attachmentCacheDir) {
		Assert.notNull(attachmentCacheDir, "'attachmentCacheDir' must not be null");
		Assert.isTrue(attachmentCacheDir.isDirectory(), "'attachmentCacheDir' must be a directory");
		Assert.isTrue(attachmentCacheDir.canWrite(), "'attachmentCacheDir' must be writable");
		this.attachmentCacheDir = attachmentCacheDir;
	}

	/**
	 * Sets the threshold for attachments caching, in bytes. Attachments larger than this
	 * threshold will be cached in the {@link #setAttachmentCacheDir(File) attachment
	 * cache directory}. Only used when {@link #setAttachmentCaching(boolean)
	 * attachmentCaching} is set to {@code true}.
	 * <p>
	 * Defaults to 4096 bytes (i.e. 4 kilobytes).
	 */
	public void setAttachmentCacheThreshold(int attachmentCacheThreshold) {
		Assert.isTrue(attachmentCacheThreshold > 0, "'attachmentCacheThreshold' must be larger than 0");
		this.attachmentCacheThreshold = attachmentCacheThreshold;
	}

	@Override
	public void setSoapVersion(SoapVersion version) {
		if (SoapVersion.SOAP_11 == version) {
			this.soapFactory = OMAbstractFactory.getSOAP11Factory();
		}
		else if (SoapVersion.SOAP_12 == version) {
			this.soapFactory = OMAbstractFactory.getSOAP12Factory();
		}
		else {
			throw new IllegalArgumentException(
					"Invalid version [" + version + "]. " + "Expected the SOAP_11 or SOAP_12 constant");
		}
	}

	/**
	 * Defines whether a {@code xml:lang} attribute should be set on SOAP 1.1
	 * {@code <faultstring>} elements.
	 * <p>
	 * The default is {@code true}, to comply with WS-I, but this flag can be set to
	 * {@code false} to the older W3C SOAP 1.1 specification.
	 * @see <a href=
	 * "http://www.ws-i.org/Profiles/BasicProfile-1.1.html#SOAP_Fault_Language">WS-I Basic
	 * Profile 1.1</a>
	 */
	public void setLangAttributeOnSoap11FaultString(boolean langAttributeOnSoap11FaultString) {
		this.langAttributeOnSoap11FaultString = langAttributeOnSoap11FaultString;
	}

	/**
	 * Sets whether internal entity references should be replaced with their replacement
	 * text and report them as characters.
	 * @see XMLInputFactory#IS_REPLACING_ENTITY_REFERENCES
	 */
	public void setReplacingEntityReferences(boolean replacingEntityReferences) {
		this.replacingEntityReferences = replacingEntityReferences;
	}

	/**
	 * Sets whether external parsed entities should be resolved.
	 * @see XMLInputFactory#IS_SUPPORTING_EXTERNAL_ENTITIES
	 */
	public void setSupportingExternalEntities(boolean supportingExternalEntities) {
		this.supportingExternalEntities = supportingExternalEntities;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (logger.isInfoEnabled()) {
			logger.info(this.payloadCaching ? "Enabled payload caching" : "Disabled payload caching");
		}
		if (this.attachmentCacheDir == null) {
			String tempDir = System.getProperty("java.io.tmpdir");
			setAttachmentCacheDir(new File(tempDir));
		}
		this.inputFactory = createXmlInputFactory();
	}

	@Override
	public AxiomSoapMessage createWebServiceMessage() {
		return new AxiomSoapMessage(this.soapFactory, this.payloadCaching, this.langAttributeOnSoap11FaultString);
	}

	@Override
	public AxiomSoapMessage createWebServiceMessage(InputStream inputStream) throws IOException {
		Assert.isInstanceOf(TransportInputStream.class, inputStream,
				"AxiomSoapMessageFactory requires a TransportInputStream");
		if (this.inputFactory == null) {
			this.inputFactory = createXmlInputFactory();
		}
		TransportInputStream transportInputStream = (TransportInputStream) inputStream;
		String contentType = getHeaderValue(transportInputStream, TransportConstants.HEADER_CONTENT_TYPE);
		if (!StringUtils.hasLength(contentType)) {
			if (logger.isDebugEnabled()) {
				logger.debug("TransportInputStream has no Content-Type header; defaulting to \""
						+ this.soapFactory.getSOAPVersion().getMediaType() + "\"");
			}
			contentType = this.soapFactory.getSOAPVersion().getMediaType().toString();
		}
		String soapAction = getHeaderValue(transportInputStream, TransportConstants.HEADER_SOAP_ACTION);
		if (!StringUtils.hasLength(soapAction)) {
			soapAction = SoapUtils.extractActionFromContentType(contentType);
		}
		try {
			if (isMultiPartRelated(contentType)) {
				return createMultiPartAxiomSoapMessage(inputStream, contentType, soapAction);
			}
			else {
				return createAxiomSoapMessage(inputStream, contentType, soapAction);
			}
		}
		catch (XMLStreamException ex) {
			throw new AxiomSoapMessageCreationException("Could not parse request: " + ex.getMessage(), ex);
		}
		catch (OMException ex) {
			throw new AxiomSoapMessageCreationException("Could not create message: " + ex.getMessage(), ex);
		}
	}

	private @Nullable String getHeaderValue(TransportInputStream transportInputStream, String header)
			throws IOException {
		String contentType = null;
		Iterator<String> iterator = transportInputStream.getHeaders(header);
		if (iterator.hasNext()) {
			contentType = iterator.next();
		}
		return contentType;
	}

	private boolean isMultiPartRelated(String contentType) {
		contentType = contentType.toLowerCase(Locale.ENGLISH);
		return contentType.contains(MULTI_PART_RELATED_CONTENT_TYPE);
	}

	/**
	 * Creates an AxiomSoapMessage without attachments.
	 */
	private AxiomSoapMessage createAxiomSoapMessage(InputStream inputStream, String contentType, String soapAction)
			throws XMLStreamException {
		SOAPModelBuilder builder = OMXMLBuilderFactory.createSOAPModelBuilder(inputStream,
				getCharSetEncoding(contentType));
		SOAPMessage soapMessage = builder.getSOAPMessage();
		return new AxiomSoapMessage(soapMessage, soapAction, this.payloadCaching,
				this.langAttributeOnSoap11FaultString);
	}

	/**
	 * Creates an AxiomSoapMessage with attachments.
	 */
	private AxiomSoapMessage createMultiPartAxiomSoapMessage(InputStream inputStream, String contentType,
			String soapAction) throws XMLStreamException {
		Attachments attachments = new Attachments(inputStream, contentType, this.attachmentCaching,
				this.attachmentCacheDir.getAbsolutePath(), Integer.toString(this.attachmentCacheThreshold));
		String charSetEncoding = getCharSetEncoding(attachments.getRootPartContentType());
		SOAPModelBuilder builder;
		if (MTOMConstants.SWA_TYPE.equals(attachments.getAttachmentSpecType())
				|| MTOMConstants.SWA_TYPE_12.equals(attachments.getAttachmentSpecType())) {
			builder = OMXMLBuilderFactory.createSOAPModelBuilder(attachments.getRootPartInputStream(), charSetEncoding);
		}
		else if (MTOMConstants.MTOM_TYPE.equals(attachments.getAttachmentSpecType())) {
			builder = OMXMLBuilderFactory.createSOAPModelBuilder(attachments.getMultipartBody());
		}
		else {
			throw new AxiomSoapMessageCreationException(
					"Unknown attachment type: [" + attachments.getAttachmentSpecType() + "]");
		}
		return new AxiomSoapMessage(builder.getSOAPMessage(), attachments, soapAction, this.payloadCaching,
				this.langAttributeOnSoap11FaultString);
	}

	/**
	 * Returns the character set from the given content type. Mostly copied
	 * @return the character set encoding
	 */
	protected String getCharSetEncoding(String contentType) {
		int charSetIdx = contentType.indexOf(CHARSET_PARAMETER);
		if (charSetIdx == -1) {
			return DEFAULT_CHARSET_ENCODING;
		}
		int eqIdx = contentType.indexOf("=", charSetIdx);

		int indexOfSemiColon = contentType.indexOf(";", eqIdx);
		String value;

		if (indexOfSemiColon > 0) {
			value = contentType.substring(eqIdx + 1, indexOfSemiColon);
		}
		else {
			value = contentType.substring(eqIdx + 1, contentType.length()).trim();
		}
		if (value.startsWith("\"")) {
			value = value.substring(1);
		}
		if (value.endsWith("\"")) {
			return value.substring(0, value.length() - 1);
		}
		if ("null".equalsIgnoreCase(value)) {
			return DEFAULT_CHARSET_ENCODING;
		}
		else {
			return value.trim();
		}
	}

	/**
	 * Create a {@code XMLInputFactory} that this resolver will use to create
	 * {@link XMLStreamReader} objects.
	 * <p>
	 * Can be overridden in subclasses, adding further initialization of the factory. The
	 * resulting factory is cached, so this method will only be called once.
	 * <p>
	 * By default this method creates a standard {@link XMLInputFactory} and configures it
	 * based on the {@link #setReplacingEntityReferences(boolean)
	 * replacingEntityReferences} and {@link #setSupportingExternalEntities(boolean)
	 * supportingExternalEntities} properties.
	 * @return the created factory
	 */
	protected XMLInputFactory createXmlInputFactory() {
		XMLInputFactory inputFactory = XMLInputFactoryUtils.newInstance();
		inputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, this.replacingEntityReferences);
		inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, this.supportingExternalEntities);
		return inputFactory;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder("AxiomSoapMessageFactory[");
		if (this.soapFactory.getSOAPVersion() == SOAPVersion.SOAP11) {
			builder.append("SOAP 1.1");
		}
		else if (this.soapFactory.getSOAPVersion() == SOAPVersion.SOAP12) {
			builder.append("SOAP 1.2");
		}
		builder.append(',');
		if (this.payloadCaching) {
			builder.append("PayloadCaching enabled");
		}
		else {
			builder.append("PayloadCaching disabled");
		}
		builder.append(']');
		return builder.toString();
	}

}
