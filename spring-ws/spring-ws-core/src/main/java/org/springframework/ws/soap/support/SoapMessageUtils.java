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

package org.springframework.ws.soap.support;

import javax.xml.namespace.QName;

import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapVersion;

/**
 * @author Arjen Poutsma
 */
public abstract class SoapMessageUtils {

    public static final String DEFAULT_MUST_UNDERSTAND_FAULT_STRING = "SOAP Must Understand Error";

    private static final QName NOT_UNDERSTOOD_HEADER_NAME = new QName(SoapVersion.SOAP_12.getEnvelopeNamespaceUri(),
            "NotUnderstood", SoapVersion.SOAP_12.getDefaultEnvelopeNamespacePrefix());

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
     */
    public static SoapFault addMustUnderstandFault(SoapMessage message, QName[] headers) {
        if (message.getVersion() == SoapVersion.SOAP_12) {
            SoapHeader header = message.getSoapHeader();
            for (int i = 0; i < headers.length; i++) {
                SoapHeaderElement headerElement = header.addHeaderElement(NOT_UNDERSTOOD_HEADER_NAME);
                headerElement
                        .addAttribute(new QName("qname"), headers[i].getPrefix() + ":" + headers[i].getLocalPart());
                headerElement.addAttribute(new QName("http://www.w3.org/2000/xmlns/", headers[i].getPrefix(), "xmlns"),
                        headers[i].getNamespaceURI());
            }
        }
        return message.getSoapBody()
                .addFault(message.getVersion().getMustUnderstandAttributeName(), DEFAULT_MUST_UNDERSTAND_FAULT_STRING);

    }

    /**
     * Adds a <code>Sender</code>/<code>Client</code> fault to the body.  If the underlying message is SOAP 1.1 based,
     * this methods creates a <code>Client</code> fault code; in SOAP 1.2, it creates a <code>Sender</code> fault code.
     * <p/>
     * Adding a fault removes the current content of the body.
     *
     * @param faultString the fault string
     * @return the created <code>SoapFault</code>
     */
    public static SoapFault addSenderFault(SoapMessage message, String faultString) {
        return message.getSoapBody().addFault(message.getVersion().getSenderFaultName(), faultString);
    }

    /**
     * Adds a <code>Receiver</code>/<code>Server</code> fault to the body.  If the underlying message is SOAP 1.1 based,
     * this methods creates a <code>Receiver</code> fault code; in SOAP 1.2, it creates a <code>Receiver</code> fault
     * code.
     * <p/>
     * Adding a fault removes the current content of the body.
     *
     * @param faultString the fault string
     * @return the created <code>SoapFault</code>
     */
    public static SoapFault addReceiverFault(SoapMessage message, String faultString) {
        return message.getSoapBody().addFault(message.getVersion().getReceiverFaultName(), faultString);
    }

}
