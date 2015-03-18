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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import javax.activation.DataHandler;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.ws.mime.Attachment;
import org.springframework.ws.mime.AttachmentException;
import org.springframework.ws.soap.AbstractSoapMessage;
import org.springframework.ws.soap.SoapEnvelope;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.saaj.support.SaajUtils;
import org.springframework.ws.soap.support.SoapUtils;
import org.springframework.ws.transport.TransportConstants;
import org.springframework.ws.transport.TransportOutputStream;

/**
 * SAAJ-specific implementation of the {@link SoapMessage} interface. Created via the {@link SaajSoapMessageFactory},
 * wraps a {@link SOAPMessage}.
 *
 * @author Arjen Poutsma
 * @see SOAPMessage
 * @since 1.0.0
 */
public class SaajSoapMessage extends AbstractSoapMessage {

	private static final String CONTENT_TYPE_XOP = "application/xop+xml";

	private final MessageFactory messageFactory;

	private SOAPMessage saajMessage;

	private SoapEnvelope envelope;

	private final boolean langAttributeOnSoap11FaultString;

	/**
	 * Create a new {@code SaajSoapMessage} based on the given SAAJ {@code SOAPMessage}.
	 *
	 * @param soapMessage the SAAJ SOAPMessage
	 */
	public SaajSoapMessage(SOAPMessage soapMessage) {
		this(soapMessage, true, null);
	}

	/**
	 * Create a new {@code SaajSoapMessage} based on the given SAAJ {@code SOAPMessage}.
	 *
	 * @param soapMessage the SAAJ SOAPMessage
	 * @param messageFactory the SAAJ message factory
	 */
	public SaajSoapMessage(SOAPMessage soapMessage, MessageFactory messageFactory) {
		this(soapMessage, true, messageFactory);
	}

	/**
	 * Create a new {@code SaajSoapMessage} based on the given SAAJ {@code SOAPMessage}.
	 *
	 * @param soapMessage the SAAJ SOAPMessage
	 * @param langAttributeOnSoap11FaultString
	 *					  whether a {@code xml:lang} attribute is allowed on SOAP 1.1 {@code <faultstring>} elements
	 */
	public SaajSoapMessage(SOAPMessage soapMessage, boolean langAttributeOnSoap11FaultString) {
		this(soapMessage, langAttributeOnSoap11FaultString, null);
	}

	/**
	 * Create a new {@code SaajSoapMessage} based on the given SAAJ {@code SOAPMessage}.
	 *
	 * @param soapMessage the SAAJ SOAPMessage
	 * @param langAttributeOnSoap11FaultString
	 *					  whether a {@code xml:lang} attribute is allowed on SOAP 1.1 {@code <faultstring>} elements
	 * @param messageFactory the message factory
	 */
	public SaajSoapMessage(SOAPMessage soapMessage, boolean langAttributeOnSoap11FaultString, MessageFactory messageFactory) {
		Assert.notNull(soapMessage, "soapMessage must not be null");
		saajMessage = soapMessage;
		this.langAttributeOnSoap11FaultString = langAttributeOnSoap11FaultString;
		MimeHeaders headers = soapMessage.getMimeHeaders();
		if (ObjectUtils.isEmpty(headers.getHeader(TransportConstants.HEADER_SOAP_ACTION))) {
			headers.addHeader(TransportConstants.HEADER_SOAP_ACTION, "\"\"");
		}
		this.messageFactory = messageFactory;
	}

	/** Return the SAAJ {@code SOAPMessage} that this {@code SaajSoapMessage} is based on. */
	public SOAPMessage getSaajMessage() {
		return saajMessage;
	}

	/** Sets the SAAJ {@code SOAPMessage} that this {@code SaajSoapMessage} is based on. */
	public void setSaajMessage(SOAPMessage soapMessage) {
		Assert.notNull(soapMessage, "soapMessage must not be null");
		saajMessage = soapMessage;
		envelope = null;
	}

	@Override
	public SoapEnvelope getEnvelope() {
		if (envelope == null) {
			try {
				SOAPEnvelope saajEnvelope = getSaajMessage().getSOAPPart().getEnvelope();
				envelope = new SaajSoapEnvelope(saajEnvelope, langAttributeOnSoap11FaultString);
			}
			catch (SOAPException ex) {
				throw new SaajSoapEnvelopeException(ex);
			}
		}
		return envelope;
	}

	@Override
	public String getSoapAction() {
		MimeHeaders mimeHeaders = getSaajMessage().getMimeHeaders();
		if (SoapVersion.SOAP_11 == getVersion()) {
			String[] actions = mimeHeaders.getHeader(TransportConstants.HEADER_SOAP_ACTION);
			return ObjectUtils.isEmpty(actions) ? TransportConstants.EMPTY_SOAP_ACTION : actions[0];
		}
		else if (SoapVersion.SOAP_12 == getVersion()) {
			String[] contentTypes = mimeHeaders.getHeader(TransportConstants.HEADER_CONTENT_TYPE);
			return !ObjectUtils.isEmpty(contentTypes) ? SoapUtils.extractActionFromContentType(contentTypes[0]) :
					TransportConstants.EMPTY_SOAP_ACTION;
		}
		else {
			throw new IllegalStateException("Unsupported SOAP version: " + getVersion());
		}
	}

	@Override
	public void setSoapAction(String soapAction) {
		MimeHeaders mimeHeaders = getSaajMessage().getMimeHeaders();
		soapAction = SoapUtils.escapeAction(soapAction);
		if (SoapVersion.SOAP_11 == getVersion()) {
			mimeHeaders.setHeader(TransportConstants.HEADER_SOAP_ACTION, soapAction);
		}
		else if (SoapVersion.SOAP_12 == getVersion()) {
			// force save of Content Type header
			try {
				saajMessage.saveChanges();
			}
			catch (SOAPException ex) {
				throw new SaajSoapMessageException("Could not save message", ex);
			}
			String[] contentTypes = mimeHeaders.getHeader(TransportConstants.HEADER_CONTENT_TYPE);
			String contentType = !ObjectUtils.isEmpty(contentTypes) ? contentTypes[0] : getVersion().getContentType();
			contentType = SoapUtils.setActionInContentType(contentType, soapAction);
			mimeHeaders.setHeader(TransportConstants.HEADER_CONTENT_TYPE, contentType);
			mimeHeaders.removeHeader(TransportConstants.HEADER_SOAP_ACTION);
		}
		else {
			throw new IllegalStateException("Unsupported SOAP version: " + getVersion());
		}

	}

	@Override
	public Document getDocument() {
		Assert.state(messageFactory != null, "Could find message factory to use");
		// return saajSoapMessage.getSaajMessage().getSOAPPart(); // does not work, see SWS-345
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			getSaajMessage().writeTo(bos);
			ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
			SOAPMessage saajMessage = messageFactory.createMessage(getSaajMessage().getMimeHeaders(), bis);
			setSaajMessage(saajMessage);
			return saajMessage.getSOAPPart();
		}
		catch (SOAPException ex) {
			throw new SaajSoapMessageException("Could not save changes", ex);
		}
		catch (IOException ex) {
			throw new SaajSoapMessageException("Could not save changes", ex);
		}
	}

	@Override
	public void setDocument(Document document) {
		if (saajMessage.getSOAPPart() != document) {
			Assert.state(messageFactory != null, "Could find message factory to use");
			try {
				DOMImplementation implementation = document.getImplementation();
				Assert.isInstanceOf(DOMImplementationLS.class, implementation);

				DOMImplementationLS loadSaveImplementation = (DOMImplementationLS) implementation;
				LSOutput output = loadSaveImplementation.createLSOutput();
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				output.setByteStream(bos);

				LSSerializer serializer = loadSaveImplementation.createLSSerializer();
				serializer.write(document, output);

				ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());

				this.saajMessage = messageFactory.createMessage(saajMessage.getMimeHeaders(), bis);

			}
			catch (SOAPException ex) {
				throw new SaajSoapMessageException("Could not read input stream", ex);
			}
			catch (IOException ex) {
				throw new SaajSoapMessageException("Could not read input stream", ex);
			}
		}
	}

	@Override
	public void writeTo(OutputStream outputStream) throws IOException {
		MimeHeaders mimeHeaders = getSaajMessage().getMimeHeaders();
		if (ObjectUtils.isEmpty(mimeHeaders.getHeader(TransportConstants.HEADER_ACCEPT))) {
			mimeHeaders.setHeader(TransportConstants.HEADER_ACCEPT, getVersion().getContentType());
		}
		try {
			SOAPMessage message = getSaajMessage();
			message.saveChanges();
			if (outputStream instanceof TransportOutputStream) {
				TransportOutputStream transportOutputStream = (TransportOutputStream) outputStream;
				// some SAAJ implementations (Axis 1) do not have a Content-Type header by default
				MimeHeaders headers = message.getMimeHeaders();
				if (ObjectUtils
						.isEmpty(
								headers.getHeader(TransportConstants.HEADER_CONTENT_TYPE))) {
					SOAPEnvelope envelope1 = message.getSOAPPart().getEnvelope();
					if (envelope1.getElementQName().getNamespaceURI()
							.equals(SoapVersion.SOAP_11.getEnvelopeNamespaceUri())) {
						headers.addHeader(TransportConstants.HEADER_CONTENT_TYPE, SoapVersion.SOAP_11.getContentType());
					}
					else {
						headers.addHeader(TransportConstants.HEADER_CONTENT_TYPE, SoapVersion.SOAP_12.getContentType());
					}
					message.saveChanges();
				}
				for (Iterator<?> iterator = headers.getAllHeaders(); iterator.hasNext();) {
					MimeHeader mimeHeader = (MimeHeader) iterator.next();
					transportOutputStream.addHeader(mimeHeader.getName(), mimeHeader.getValue());
				}
			}
			message.writeTo(outputStream);

			outputStream.flush();
		}
		catch (SOAPException ex) {
			throw new SaajSoapMessageException("Could not write message to OutputStream: " + ex.getMessage(), ex);
		}
	}

	@Override
	public boolean isXopPackage() {
		SOAPPart saajPart = saajMessage.getSOAPPart();
		String[] contentTypes = saajPart.getMimeHeader(TransportConstants.HEADER_CONTENT_TYPE);
		for (String contentType : contentTypes) {
			if (contentType.contains(CONTENT_TYPE_XOP)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean convertToXopPackage() {
		convertMessageToXop();
		convertPartToXop();
		return true;
	}

	private void convertMessageToXop() {
		MimeHeaders mimeHeaders = saajMessage.getMimeHeaders();
		String[] oldContentTypes = mimeHeaders.getHeader(TransportConstants.HEADER_CONTENT_TYPE);
		String oldContentType =
				!ObjectUtils.isEmpty(oldContentTypes) ? oldContentTypes[0] : getVersion().getContentType();
		mimeHeaders.setHeader(TransportConstants.HEADER_CONTENT_TYPE,
				CONTENT_TYPE_XOP + ";type=" + '"' + oldContentType + '"');
	}

	private void convertPartToXop() {
		SOAPPart saajPart = saajMessage.getSOAPPart();
		String[] oldContentTypes = saajPart.getMimeHeader(TransportConstants.HEADER_CONTENT_TYPE);
		String oldContentType =
				!ObjectUtils.isEmpty(oldContentTypes) ? oldContentTypes[0] : getVersion().getContentType();
		saajPart.setMimeHeader(TransportConstants.HEADER_CONTENT_TYPE,
				CONTENT_TYPE_XOP + ";type=" + '"' + oldContentType + '"');
	}

	@Override
	@SuppressWarnings("unchecked")
	public Iterator<Attachment> getAttachments() throws AttachmentException {
		Iterator<AttachmentPart> iterator = getSaajMessage().getAttachments();
		return new SaajAttachmentIterator(iterator);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Attachment getAttachment(String contentId) {
		Assert.hasLength(contentId, "contentId must not be empty");
		MimeHeaders mimeHeaders = new MimeHeaders();
		mimeHeaders.setHeader(TransportConstants.HEADER_CONTENT_ID, contentId);
		Iterator<AttachmentPart> iterator = getSaajMessage().getAttachments(mimeHeaders);
		if (!iterator.hasNext()) {
			return null;
		}
		AttachmentPart saajAttachment = iterator.next();
		return new SaajAttachment(saajAttachment);
	}

	@Override
	public Attachment addAttachment(String contentId, DataHandler dataHandler) {
		Assert.hasLength(contentId, "contentId must not be empty");
		Assert.notNull(dataHandler, "dataHandler must not be null");
		SOAPMessage message = getSaajMessage();
		AttachmentPart attachmentPart = message.createAttachmentPart(dataHandler);
		message.addAttachmentPart(attachmentPart);
		attachmentPart.setContentId(contentId);
		attachmentPart.setMimeHeader(TransportConstants.HEADER_CONTENT_TRANSFER_ENCODING,
				"binary");
		return new SaajAttachment(attachmentPart);
	}

	public String toString() {
		StringBuilder builder = new StringBuilder("SaajSoapMessage");
		try {
			SOAPEnvelope envelope = saajMessage.getSOAPPart().getEnvelope();
			if (envelope != null) {
				SOAPBody body = envelope.getBody();
				if (body != null) {
					SOAPElement bodyElement = SaajUtils.getFirstBodyElement(body);
					if (bodyElement != null) {
						builder.append(' ');
						builder.append(bodyElement.getElementQName());
					}
				}
			}
		}
		catch (SOAPException ex) {
			// ignore
		}
		return builder.toString();
	}

	private static class SaajAttachmentIterator implements Iterator<Attachment> {

		private final Iterator<AttachmentPart> saajIterator;

		private SaajAttachmentIterator(Iterator<AttachmentPart> saajIterator) {
			this.saajIterator = saajIterator;
		}

		@Override
		public boolean hasNext() {
			return saajIterator.hasNext();
		}

		@Override
		public Attachment next() {
			AttachmentPart saajAttachment = saajIterator.next();
			return new SaajAttachment(saajAttachment);
		}

		@Override
		public void remove() {
			saajIterator.remove();
		}
	}

}
