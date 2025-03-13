/*
 * Copyright 2005-2025 the original author or authors.
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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Iterator;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.dom.DOMSource;

import jakarta.activation.DataHandler;
import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.impl.MTOMConstants;
import org.apache.axiom.om.impl.OMMultipartWriter;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPMessage;
import org.apache.axiom.soap.SOAPProcessingException;
import org.w3c.dom.Document;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.mime.Attachment;
import org.springframework.ws.soap.AbstractSoapMessage;
import org.springframework.ws.soap.SoapEnvelope;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.axiom.support.AxiomUtils;
import org.springframework.ws.soap.support.SoapUtils;
import org.springframework.ws.stream.StreamingPayload;
import org.springframework.ws.stream.StreamingWebServiceMessage;
import org.springframework.ws.transport.TransportConstants;
import org.springframework.ws.transport.TransportOutputStream;

/**
 * AXIOM-specific implementation of the {@link SoapMessage} interface. Created via the
 * {@link AxiomSoapMessageFactory}, wraps a {@link SOAPMessage}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see SOAPMessage
 */
public class AxiomSoapMessage extends AbstractSoapMessage implements StreamingWebServiceMessage {

	private static final String EMPTY_SOAP_ACTION = "\"\"";

	private SOAPMessage axiomMessage;

	private final SOAPFactory axiomFactory;

	private final Attachments attachments;

	private final boolean payloadCaching;

	private AxiomSoapEnvelope envelope;

	private String soapAction;

	private final boolean langAttributeOnSoap11FaultString;

	private OMOutputFormat outputFormat;

	/**
	 * Create a new, empty {@code AxiomSoapMessage}.
	 * @param soapFactory the AXIOM SOAPFactory
	 */
	public AxiomSoapMessage(SOAPFactory soapFactory) {
		this(soapFactory, true, true);
	}

	/**
	 * Create a new, empty {@code AxiomSoapMessage}.
	 * @param soapFactory the AXIOM SOAPFactory
	 */
	public AxiomSoapMessage(SOAPFactory soapFactory, boolean payloadCaching, boolean langAttributeOnSoap11FaultString) {
		SOAPEnvelope soapEnvelope = soapFactory.getDefaultEnvelope();
		this.axiomFactory = soapFactory;
		this.axiomMessage = this.axiomFactory.createSOAPMessage();
		this.axiomMessage.setSOAPEnvelope(soapEnvelope);
		this.attachments = new Attachments();
		this.payloadCaching = payloadCaching;
		this.langAttributeOnSoap11FaultString = langAttributeOnSoap11FaultString;
		this.soapAction = EMPTY_SOAP_ACTION;
	}

	/**
	 * Create a new {@code AxiomSoapMessage} based on the given AXIOM {@code SOAPMessage}.
	 * @param soapMessage the AXIOM SOAPMessage
	 * @param soapAction the value of the SOAP Action header
	 * @param payloadCaching whether the contents of the SOAP body should be cached or not
	 */
	public AxiomSoapMessage(SOAPMessage soapMessage, String soapAction, boolean payloadCaching,
			boolean langAttributeOnSoap11FaultString) {
		this(soapMessage, new Attachments(), soapAction, payloadCaching, langAttributeOnSoap11FaultString);
	}

	/**
	 * Create a new {@code AxiomSoapMessage} based on the given AXIOM {@code SOAPMessage}
	 * and attachments.
	 * @param soapMessage the AXIOM SOAPMessage
	 * @param attachments the attachments
	 * @param soapAction the value of the SOAP Action header
	 * @param payloadCaching whether the contents of the SOAP body should be cached or not
	 */
	public AxiomSoapMessage(SOAPMessage soapMessage, Attachments attachments, String soapAction, boolean payloadCaching,
			boolean langAttributeOnSoap11FaultString) {
		Assert.notNull(soapMessage, "'soapMessage' must not be null");
		Assert.notNull(attachments, "'attachments' must not be null");
		this.axiomMessage = soapMessage;
		this.axiomFactory = (SOAPFactory) soapMessage.getSOAPEnvelope().getOMFactory();
		this.attachments = attachments;
		if (!StringUtils.hasLength(soapAction)) {
			soapAction = EMPTY_SOAP_ACTION;
		}
		this.soapAction = soapAction;
		this.payloadCaching = payloadCaching;
		this.langAttributeOnSoap11FaultString = langAttributeOnSoap11FaultString;
	}

	/**
	 * Return the AXIOM {@code SOAPMessage} that this {@code AxiomSoapMessage} is based
	 * on.
	 */
	public final SOAPMessage getAxiomMessage() {
		return this.axiomMessage;
	}

	/**
	 * Sets the AXIOM {@code SOAPMessage} that this {@code AxiomSoapMessage} is based on.
	 * <p>
	 * Calling this method also clears the SOAP Action property.
	 */
	public final void setAxiomMessage(SOAPMessage axiomMessage) {
		Assert.notNull(axiomMessage, "'axiomMessage' must not be null");
		this.axiomMessage = axiomMessage;
		this.envelope = null;
		this.soapAction = EMPTY_SOAP_ACTION;
	}

	/**
	 * Sets the {@link OMOutputFormat} to be used when writing the message.
	 * @see #writeTo(java.io.OutputStream)
	 */
	public void setOutputFormat(OMOutputFormat outputFormat) {
		this.outputFormat = outputFormat;
	}

	@Override
	public void setStreamingPayload(StreamingPayload payload) {
		AxiomSoapBody soapBody = (AxiomSoapBody) getSoapBody();
		soapBody.setStreamingPayload(payload);
	}

	@Override
	public SoapEnvelope getEnvelope() {
		if (this.envelope == null) {
			try {
				this.envelope = new AxiomSoapEnvelope(this.axiomMessage.getSOAPEnvelope(), this.axiomFactory,
						this.payloadCaching, this.langAttributeOnSoap11FaultString);
			}
			catch (SOAPProcessingException ex) {
				throw new AxiomSoapEnvelopeException(ex);
			}
		}
		return this.envelope;
	}

	@Override
	public String getSoapAction() {
		return this.soapAction;
	}

	@Override
	public void setSoapAction(String soapAction) {
		soapAction = SoapUtils.escapeAction(soapAction);
		this.soapAction = soapAction;
	}

	@Override
	public Document getDocument() {
		return AxiomUtils.toDocument(this.axiomMessage.getSOAPEnvelope());
	}

	@Override
	public void setDocument(Document document) {
		// save the Soap Action
		String soapAction = getSoapAction();
		// replace the Axiom message
		setAxiomMessage(
				OMXMLBuilderFactory.createSOAPModelBuilder(this.axiomFactory.getMetaFactory(), new DOMSource(document))
					.getSOAPMessage());
		// restore the Soap Action
		setSoapAction(soapAction);
	}

	@Override
	public boolean isXopPackage() {
		try {
			return MTOMConstants.MTOM_TYPE.equals(this.attachments.getAttachmentSpecType());
		}
		catch (OMException ex) {
			return false;
		}
		catch (NullPointerException ex) {
			// gotta love Axis2
			return false;
		}
	}

	@Override
	public boolean convertToXopPackage() {
		return false;
	}

	@Override
	public Attachment getAttachment(String contentId) {
		Assert.hasLength(contentId, "contentId must not be empty");
		if (contentId.startsWith("<") && contentId.endsWith(">")) {
			contentId = contentId.substring(1, contentId.length() - 1);
		}
		DataHandler dataHandler = this.attachments.getDataHandler(contentId);
		return (dataHandler != null) ? new AxiomAttachment(contentId, dataHandler) : null;
	}

	@Override
	public Iterator<Attachment> getAttachments() {
		return new AxiomAttachmentIterator();
	}

	@Override
	public Attachment addAttachment(String contentId, DataHandler dataHandler) {
		Assert.hasLength(contentId, "contentId must not be empty");
		Assert.notNull(dataHandler, "dataHandler must not be null");
		this.attachments.addDataHandler(contentId, dataHandler);
		return new AxiomAttachment(contentId, dataHandler);
	}

	@Override
	public void writeTo(OutputStream outputStream) throws IOException {
		try {

			OMOutputFormat outputFormat = getOutputFormat();
			if (outputStream instanceof TransportOutputStream) {
				TransportOutputStream transportOutputStream = (TransportOutputStream) outputStream;
				String contentType = outputFormat.getContentType();
				if (!(outputFormat.isDoingSWA() || outputFormat.isOptimized())) {
					String charsetEncoding = this.axiomMessage.getCharsetEncoding();
					contentType += "; charset=" + charsetEncoding;
				}
				SoapVersion version = getVersion();
				if (SoapVersion.SOAP_11 == version) {
					transportOutputStream.addHeader(TransportConstants.HEADER_SOAP_ACTION, this.soapAction);
					transportOutputStream.addHeader(TransportConstants.HEADER_ACCEPT, version.getContentType());
				}
				else if (SoapVersion.SOAP_12 == version) {
					contentType += "; action=" + this.soapAction;
					transportOutputStream.addHeader(TransportConstants.HEADER_ACCEPT, version.getContentType());
				}
				transportOutputStream.addHeader(TransportConstants.HEADER_CONTENT_TYPE, contentType);

			}
			if (!(outputFormat.isOptimized()) & outputFormat.isDoingSWA()) {
				writeSwAMessage(outputStream, outputFormat);
			}
			else {
				if (this.payloadCaching) {
					this.axiomMessage.serialize(outputStream, outputFormat);
				}
				else {
					this.axiomMessage.serializeAndConsume(outputStream, outputFormat);
				}
			}
			outputStream.flush();
		}
		catch (XMLStreamException ex) {
			throw new AxiomSoapMessageException("Could not write message to OutputStream: " + ex.getMessage(), ex);
		}
		catch (OMException ex) {
			throw new AxiomSoapMessageException("Could not write message to OutputStream: " + ex.getMessage(), ex);
		}
	}

	private OMOutputFormat getOutputFormat() {
		if (this.outputFormat != null) {
			return this.outputFormat;
		}
		else {
			String charsetEncoding = this.axiomMessage.getCharsetEncoding();

			OMOutputFormat outputFormat = new OMOutputFormat();
			outputFormat.setCharSetEncoding(charsetEncoding);
			outputFormat.setSOAP11(getVersion() == SoapVersion.SOAP_11);
			if (isXopPackage()) {
				outputFormat.setDoOptimize(true);
			}
			else if (!this.attachments.getContentIDSet().isEmpty()) {
				outputFormat.setDoingSWA(true);
			}
			return outputFormat;
		}
	}

	private void writeSwAMessage(OutputStream outputStream, OMOutputFormat format)
			throws XMLStreamException, UnsupportedEncodingException {
		StringWriter writer = new StringWriter();
		SOAPEnvelope envelope = this.axiomMessage.getSOAPEnvelope();
		if (this.payloadCaching) {
			envelope.serialize(writer, format);
		}
		else {
			envelope.serializeAndConsume(writer, format);
		}

		try {
			OMMultipartWriter mpw = new OMMultipartWriter(outputStream, format);

			Writer rootPartWriter = new OutputStreamWriter(mpw.writeRootPart(), format.getCharSetEncoding());
			rootPartWriter.write(writer.toString());
			rootPartWriter.close();

			// Get the collection of ids associated with the attachments
			for (String id : this.attachments.getAllContentIDs()) {
				mpw.writePart(this.attachments.getBlob(id), id);
			}

			mpw.complete();
		}
		catch (IOException ex) {
			throw new OMException("Error writing SwA message", ex);
		}
	}

	public String toString() {
		StringBuilder builder = new StringBuilder("AxiomSoapMessage");
		if (this.payloadCaching) {
			try {
				SOAPEnvelope envelope = this.axiomMessage.getSOAPEnvelope();
				if (envelope != null) {
					SOAPBody body = envelope.getBody();
					if (body != null) {
						OMElement bodyElement = body.getFirstElement();
						if (bodyElement != null) {
							builder.append(' ');
							builder.append(bodyElement.getQName());
						}
					}
				}
			}
			catch (OMException ex) {
				// ignore
			}
		}
		return builder.toString();
	}

	private final class AxiomAttachmentIterator implements Iterator<Attachment> {

		private final Iterator<String> iterator;

		private AxiomAttachmentIterator() {
			this.iterator = AxiomSoapMessage.this.attachments.getContentIDSet().iterator();
		}

		@Override
		public boolean hasNext() {
			return this.iterator.hasNext();
		}

		@Override
		public Attachment next() {
			String contentId = this.iterator.next();
			DataHandler dataHandler = AxiomSoapMessage.this.attachments.getDataHandler(contentId);
			return new AxiomAttachment(contentId, dataHandler);
		}

		@Override
		public void remove() {
			this.iterator.remove();
		}

	}

}
