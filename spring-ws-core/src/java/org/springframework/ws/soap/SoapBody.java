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

import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

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
 */
public interface SoapBody extends SoapElement {

    /**
     * Returns a <code>Source</code> that represents the contents of the body.
     *
     * @return the message contents
     */
    Source getPayloadSource();

    /**
     * Returns a <code>Result</code> that represents the contents of the body.
     *
     * @return the messaage contents
     */
    Result getPayloadResult();

    /**
     * Adds a <code>MustUnderstand</code> fault to the body. A <code>MustUnderstand</code> is returned when a SOAP
     * header with a <code>MustUnderstand</code> attribute is not understood.
     * <p/>
     * Adding a fault removes the current content of the body.
     * <p/>
     * The specified headers can be used to generate an fault string.
     *
     * @param headers the qualified names of the headers that are not understood.
     * @return the created <code>SoapFault</code>
     * @see SoapFault#isMustUnderstandFault()
     */
    SoapFault addMustUnderstandFault(QName[] headers);

    /**
     * Adds a <code>Sender</code>/<code>Client</code> fault to the body.  If the underlying message is SOAP 1.1 based,
     * this methods creates a <code>Client</code> fault code; in SOAP 1.2, it creates a <code>Sender</code> fault code.
     * <p/>
     * Adding a fault removes the current content of the body.
     *
     * @param faultString the fault string
     * @return the created <code>SoapFault</code>
     * @see SoapFault#isMustUnderstandFault()
     */
    SoapFault addSenderFault(String faultString);

    /**
     * Adds a <code>Receiver</code>/<code>Server</code> fault to the body.  If the underlying message is SOAP 1.1 based,
     * this methods creates a <code>Receiver</code> fault code; in SOAP 1.2, it creates a <code>Receiver</code> fault
     * code.
     * <p/>
     * Adding a fault removes the current content of the body.
     *
     * @param faultString the fault string
     * @return the created <code>SoapFault</code>
     * @see SoapFault#isMustUnderstandFault()
     */
    SoapFault addReceiverFault(String faultString);

    /**
     * Adds a custom soap fault to the body. The given fault code must be fully qualified (i.e. namespace, prefix, and
     * local part must be present).
     * <p/>
     * Adding a fault removes the current content of the body.
     *
     * @param faultCode the fully qualified fault code
     * @return the added <code>SoapFault</code>
     * @throws IllegalArgumentException if the fault code is not fully qualified
     */
    SoapFault addFault(QName faultCode, String faultString);

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
