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

package org.springframework.ws.soap.axiom;

import java.util.Locale;
import javax.xml.namespace.QName;

import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultReason;
import org.apache.axiom.soap.SOAPFaultText;
import org.apache.axiom.soap.SOAPFaultValue;
import org.apache.axiom.soap.SOAPProcessingException;
import org.springframework.util.Assert;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.axiom.support.AxiomUtils;
import org.springframework.ws.soap.soap12.Soap12Body;
import org.springframework.ws.soap.soap12.Soap12Fault;

/**
 * Axiom-specific version of <code>org.springframework.ws.soap.Soap12Body</code>.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
class AxiomSoap12Body extends AxiomSoapBody implements Soap12Body {

    AxiomSoap12Body(SOAPBody axiomBody, SOAPFactory axiomFactory, boolean payloadCaching) {
        super(axiomBody, axiomFactory, payloadCaching);
    }

    public SoapFault addMustUnderstandFault(String reason, Locale locale) {
        Assert.notNull(locale, "No locale given");
        SOAPFault fault = addStandardFault(SOAP12Constants.FAULT_CODE_MUST_UNDERSTAND, reason, locale);
        return new AxiomSoap12Fault(fault, getAxiomFactory());
    }

    public SoapFault addClientOrSenderFault(String reason, Locale locale) {
        Assert.notNull(locale, "No locale given");
        SOAPFault fault = addStandardFault(SOAP12Constants.FAULT_CODE_SENDER, reason, locale);
        return new AxiomSoap12Fault(fault, getAxiomFactory());
    }

    public SoapFault addServerOrReceiverFault(String reason, Locale locale) {
        Assert.notNull(locale, "No locale given");
        SOAPFault fault = addStandardFault(SOAP12Constants.FAULT_CODE_RECEIVER, reason, locale);
        return new AxiomSoap12Fault(fault, getAxiomFactory());
    }

    public SoapFault addVersionMismatchFault(String reason, Locale locale) {
        Assert.notNull(locale, "No locale given");
        SOAPFault fault = addStandardFault(SOAP12Constants.FAULT_CODE_VERSION_MISMATCH, reason, locale);
        return new AxiomSoap12Fault(fault, getAxiomFactory());
    }

    public Soap12Fault addDataEncodingUnknownFault(QName[] subcodes, String reason, Locale locale) {
        Assert.notNull(locale, "No locale given");
        SOAPFault fault = addStandardFault(SOAP12Constants.FAULT_CODE_DATA_ENCODING_UNKNOWN, reason, locale);
        return new AxiomSoap12Fault(fault, getAxiomFactory());
    }

    private SOAPFault addStandardFault(String localName, String faultStringOrReason, Locale locale) {
        Assert.notNull(faultStringOrReason, "No faultStringOrReason given");
        try {
            detachAllBodyChildren();
            SOAPFault fault = getAxiomFactory().createSOAPFault(getAxiomBody());
            SOAPFaultCode code = getAxiomFactory().createSOAPFaultCode(fault);
            SOAPFaultValue value = getAxiomFactory().createSOAPFaultValue(code);
            value.setText(fault.getNamespace().getPrefix() + ":" + localName);
            SOAPFaultReason reason = getAxiomFactory().createSOAPFaultReason(fault);
            SOAPFaultText text = getAxiomFactory().createSOAPFaultText(reason);
            if (locale != null) {
                text.setLang(AxiomUtils.toLanguage(locale));
            }
            text.setText(faultStringOrReason);
            return fault;
        }
        catch (SOAPProcessingException ex) {
            throw new AxiomSoapFaultException(ex);
        }
    }


}
