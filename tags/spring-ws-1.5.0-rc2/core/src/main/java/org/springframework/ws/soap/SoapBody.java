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

package org.springframework.ws.soap;

import java.util.Locale;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.springframework.ws.WebServiceMessage;

/**
 * Represents the <code>Body</code> element in a SOAP message. A SOAP body contains the <strong>payload</strong> of the
 * message. This payload can be custom XML, or a <code>SoapFault</code> (but not both).
 * <p/>
 * Note that the source returned by <code>getSource()</code> includes the SOAP Body element itself. For the contents of
 * the body, use <code>getPayloadSource()</code>.
 *
 * @author Arjen Poutsma
 * @see SoapEnvelope#getBody()
 * @see #getPayloadSource()
 * @see #getPayloadResult()
 * @see SoapFault
 * @since 1.0.0
 */
public interface SoapBody extends SoapElement {

    /**
     * Returns a <code>Source</code> that represents the contents of the body.
     *
     * @return the message contents
     * @see WebServiceMessage#getPayloadSource()
     */
    Source getPayloadSource();

    /**
     * Returns a <code>Result</code> that represents the contents of the body.
     * <p/>
     * Calling this method removes the current content of the body.
     *
     * @return the message contents
     * @see WebServiceMessage#getPayloadResult()
     */
    Result getPayloadResult();

    /**
     * Adds a <code>MustUnderstand</code> fault to the body. A <code>MustUnderstand</code> is returned when a SOAP
     * header with a <code>MustUnderstand</code> attribute is not understood.
     * <p/>
     * Adding a fault removes the current content of the body.
     *
     * @param faultStringOrReason the SOAP 1.1 fault string or SOAP 1.2 reason text
     * @param locale              the language of faultStringOrReason. Optional for SOAP 1.1
     * @return the created <code>SoapFault</code>
     */
    SoapFault addMustUnderstandFault(String faultStringOrReason, Locale locale) throws SoapFaultException;

    /**
     * Adds a <code>Client</code>/<code>Sender</code> fault to the body. For SOAP 1.1, this adds a fault with a
     * <code>Client</code> fault code. For SOAP 1.2, this adds a fault with a <code>Sender</code> code.
     * <p/>
     * Adding a fault removes the current content of the body.
     *
     * @param faultStringOrReason the SOAP 1.1 fault string or SOAP 1.2 reason text
     * @param locale              the language of faultStringOrReason. Optional for SOAP 1.1
     * @return the created <code>SoapFault</code>
     */
    SoapFault addClientOrSenderFault(String faultStringOrReason, Locale locale) throws SoapFaultException;

    /**
     * Adds a <code>Server</code>/<code>Receiver</code> fault to the body. For SOAP 1.1, this adds a fault with a
     * <code>Server</code> fault code. For SOAP 1.2, this adds a fault with a <code>Receiver</code> code.
     * <p/>
     * Adding a fault removes the current content of the body.
     *
     * @param faultStringOrReason the SOAP 1.1 fault string or SOAP 1.2 reason text
     * @param locale              the language of faultStringOrReason. Optional for SOAP 1.1
     * @return the created <code>SoapFault</code>
     */
    SoapFault addServerOrReceiverFault(String faultStringOrReason, Locale locale) throws SoapFaultException;

    /**
     * Adds a <code>VersionMismatch</code> fault to the body.
     * <p/>
     * Adding a fault removes the current content of the body.
     *
     * @param faultStringOrReason the SOAP 1.1 fault string or SOAP 1.2 reason text
     * @param locale              the language of faultStringOrReason. Optional for SOAP 1.1
     * @return the created <code>SoapFault</code>
     */
    SoapFault addVersionMismatchFault(String faultStringOrReason, Locale locale) throws SoapFaultException;

    /**
     * Indicates whether this body has a <code>SoapFault</code>.
     *
     * @return <code>true</code> if the body has a fault; <code>false</code> otherwise
     */
    boolean hasFault();

    /**
     * Returns the <code>SoapFault</code> of this body.
     *
     * @return the <code>SoapFault</code>, or <code>null</code> if none is present
     */
    SoapFault getFault();
}
