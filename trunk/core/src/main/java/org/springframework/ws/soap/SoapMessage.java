/*
 * Copyright 2005 the original author or authors.
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

package org.springframework.ws.soap;

import java.io.File;
import java.util.Iterator;

import org.springframework.core.io.InputStreamSource;
import org.springframework.ws.WebServiceMessage;

/**
 * Represents an abstraction for SOAP messages, providing access to a SOAP Envelope. The contents of the SOAP body can
 * be retrieved by <code>getPayloadSource()</code> and <code>getPayloadResult()</code> on
 * <code>WebServiceMessage</code>, the super-interface of this interface.
 *
 * @author Arjen Poutsma
 * @see #getPayloadSource()
 * @see #getPayloadResult()
 * @see #getEnvelope()
 */
public interface SoapMessage extends WebServiceMessage {

    /**
     * Returns the <code>SoapEnvelope</code> associated with this <code>SoapMessage</code>.
     */
    SoapEnvelope getEnvelope() throws SoapEnvelopeException;

    /**
     * Returns the <code>SoapBody</code> associated with this <code>SoapMessage</code>. This is a convenience method for
     * <code>getEnvelope().getBody()</code>.
     *
     * @see SoapEnvelope#getBody()
     */
    SoapBody getSoapBody() throws SoapBodyException;

    /**
     * Returns the <code>SoapHeader</code> associated with this <code>SoapMessage</code>. This is a convenience method
     * for <code>getEnvelope().getHeader()</code>.
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
     * Returns the <code>Attachment</code> with the specified content Id.
     *
     * @return the attachment with the specified content id; or <code>null</code> if it cannot be found
     * @throws AttachmentException in case of errors
     */
    Attachment getAttachment(String contentId) throws AttachmentException;

    /**
     * Returns an <code>Iterator</code> over all <code>Attachment</code>s that are part of this
     * <code>SoapMessage</code>.
     *
     * @return an iterator over all attachments
     * @throws AttachmentException in case of errors
     * @see Attachment
     */
    Iterator getAttachments() throws AttachmentException;

    /**
     * Add an attachment to the <code>SoapMessage</code>, taking the content from a <code>java.io.File</code>.
     * <p/>
     * The content type will be determined by the name of the given content file. Do not use this for temporary files
     * with arbitrary filenames (possibly ending in ".tmp" or the like)!
     *
     * @param file the File resource to take the content from
     * @return the added attachment
     * @throws AttachmentException in case of errors
     * @see #addAttachment(InputStreamSource, String)
     */
    Attachment addAttachment(File file) throws AttachmentException;

    /**
     * Add an attachment to the <code>SoapMessage</code>, taking the content from an
     * <code>org.springframework.core.io.InputStreamResource</code>.
     * <p/>
     * Note that the <code>InputStream</code> returned by the source needs to be a <em>fresh one on each call</em>, as
     * underlying implementations can invoke <code>getInputStream()</code> multiple times.
     *
     * @param inputStreamSource the resource to take the content from (all of Spring's Resource implementations can be
     *                          passed in here)
     * @param contentType       the content type to use for the element
     * @return the added attachment
     * @throws AttachmentException in case of errors
     * @see #addAttachment(java.io.File)
     * @see org.springframework.core.io.Resource
     */
    Attachment addAttachment(InputStreamSource inputStreamSource, String contentType);

}
