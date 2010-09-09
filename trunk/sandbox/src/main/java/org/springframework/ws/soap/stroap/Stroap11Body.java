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

package org.springframework.ws.soap.stroap;

import java.util.Locale;
import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;

import org.springframework.util.Assert;
import org.springframework.ws.soap.SoapFaultException;
import org.springframework.ws.soap.soap11.Soap11Body;
import org.springframework.ws.soap.soap11.Soap11Fault;

/**
 * @author Arjen Poutsma
 */
class Stroap11Body extends StroapBody implements Soap11Body {

    private static final String ENVELOPE_NAMESPACE_URI = "http://schemas.xmlsoap.org/soap/envelope/";

    private QName CLIENT_FAULT_NAME = new QName(ENVELOPE_NAMESPACE_URI, "Client", PREFIX);

    private QName SERVER_FAULT_NAME = new QName(ENVELOPE_NAMESPACE_URI, "Server", PREFIX);

    private QName MUST_UNDERSTAND_FAULT_NAME = new QName(ENVELOPE_NAMESPACE_URI, "MustUnderstand", PREFIX);

    private QName VERSION_MISMATCH_FAULT_NAME = new QName(ENVELOPE_NAMESPACE_URI, "VersionMismatch", PREFIX);

    Stroap11Body(StroapMessageFactory messageFactory) {
        super(messageFactory);
    }

    Stroap11Body(StartElement startElement, StroapPayload payload, StroapMessageFactory messageFactory) {
        super(startElement, payload, messageFactory);
    }

    @Override
    public Soap11Fault getFault() {
        return (Soap11Fault) super.getFault();
    }

    public Soap11Fault addMustUnderstandFault(String faultStringOrReason, Locale locale) throws SoapFaultException {
        Stroap11Fault fault =
                new Stroap11Fault(MUST_UNDERSTAND_FAULT_NAME, "SOAP Must Understand Error", null, getMessageFactory());
        setFault(fault);
        return fault;
    }

    public Soap11Fault addClientOrSenderFault(String faultStringOrReason, Locale locale) throws SoapFaultException {
        Assert.hasLength(faultStringOrReason, "'faultStringOrReason' must not be empty");
        Stroap11Fault fault = new Stroap11Fault(CLIENT_FAULT_NAME, faultStringOrReason, null, getMessageFactory());
        setFault(fault);
        return fault;
    }

    public Soap11Fault addServerOrReceiverFault(String faultStringOrReason, Locale locale) throws SoapFaultException {
        Assert.hasLength(faultStringOrReason, "'faultStringOrReason' must not be empty");
        Stroap11Fault fault = new Stroap11Fault(SERVER_FAULT_NAME, faultStringOrReason, null, getMessageFactory());
        setFault(fault);
        return fault;
    }

    public Soap11Fault addVersionMismatchFault(String faultStringOrReason, Locale locale) throws SoapFaultException {
        Assert.hasLength(faultStringOrReason, "'faultStringOrReason' must not be empty");
        Stroap11Fault fault =
                new Stroap11Fault(VERSION_MISMATCH_FAULT_NAME, faultStringOrReason, null, getMessageFactory());
        setFault(fault);
        return fault;
    }

    public Soap11Fault addFault(QName faultCode, String faultString, Locale faultStringLocale)
            throws SoapFaultException {
        Assert.notNull(faultCode, "'faultCode' must not be null");
        Assert.hasLength(faultCode.getLocalPart(), "faultCode's localPart cannot be empty");
        Assert.hasLength(faultCode.getNamespaceURI(), "faultCode's namespaceUri cannot be empty");
        Assert.hasLength(faultString, "'faultString' must not be empty");

        Stroap11Fault fault = new Stroap11Fault(faultCode, faultString, faultStringLocale, getMessageFactory());
        setFault(fault);
        return fault;
    }
}
