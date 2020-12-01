/*
 * Copyright 2005-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.soap;

import org.springframework.ws.FaultAwareWebServiceMessage;
import org.springframework.ws.mime.MimeMessage;
import org.w3c.dom.Document;

/**
 * Represents an abstraction for SOAP messages, providing access to a SOAP Envelope. The contents of the SOAP body can
 * be retrieved by {@code getPayloadSource()} and {@code getPayloadResult()} on {@code WebServiceMessage}, the
 * super-interface of this interface.
 *
 * @author Arjen Poutsma
 * @see #getPayloadSource()
 * @see #getPayloadResult()
 * @see #getEnvelope()
 * @since 1.0.0
 */
public interface SoapMessage extends MimeMessage, FaultAwareWebServiceMessage {

	/** Returns the {@code SoapEnvelope} associated with this {@code SoapMessage}. */
	SoapEnvelope getEnvelope() throws SoapEnvelopeException;

	/**
	 * Get the SOAP Action for this message, or {@code null} if not present.
	 *
	 * @return the SOAP Action.
	 */
	String getSoapAction();

	/**
	 * Sets the SOAP Action for this message.
	 *
	 * @param soapAction the SOAP Action.
	 */
	void setSoapAction(String soapAction);

	/**
	 * Returns the {@code SoapBody} associated with this {@code SoapMessage}. This is a convenience method for
	 * {@code getEnvelope().getBody()}.
	 *
	 * @see SoapEnvelope#getBody()
	 */
	SoapBody getSoapBody() throws SoapBodyException;

	/**
	 * Returns the {@code SoapHeader} associated with this {@code SoapMessage}. This is a convenience method for
	 * {@code getEnvelope().getHeader()}.
	 *
	 * @see SoapEnvelope#getHeader()
	 */
	SoapHeader getSoapHeader() throws SoapHeaderException;

	/**
	 * Returns the SOAP version of this message. This can be either SOAP 1.1 or SOAP 1.2.
	 *
	 * @return the SOAP version
	 * @see SoapVersion#SOAP_11
	 * @see SoapVersion#SOAP_12
	 */
	SoapVersion getVersion();

	/**
	 * Returns this message as a {@link Document}. Depending on the underlying implementation, this Document may be 'live'
	 * or not.
	 * 
	 * @return this soap message as a DOM document
	 */
	Document getDocument();

	/**
	 * Sets the contents of the message to the given {@link Document}.
	 *
	 * @param document the soap message as a DOM document
	 */
	void setDocument(Document document);

}
