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

import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultText;
import org.springframework.ws.soap.axiom.support.AxiomUtils;
import org.springframework.ws.soap.soap11.Soap11Fault;

/**
 * Axiom-specific version of <code>org.springframework.ws.soap.Soap11Fault</code>.
 *
 * @author Arjen Poutsma
 */
class AxiomSoap11Fault extends AxiomSoapFault implements Soap11Fault {

    AxiomSoap11Fault(SOAPFault axiomFault, SOAPFactory axiomFactory) {
        super(axiomFault, axiomFactory);
    }

    public String getFaultString() {
        if (axiomFault.getReason() != null) {
            SOAPFaultText soapText = axiomFault.getReason().getFirstSOAPText();
            if (soapText != null) {
                return soapText.getText();
            }
        }
        return null;
    }

    public Locale getFaultStringLocale() {
        if (axiomFault.getReason() != null) {
            SOAPFaultText soapText = axiomFault.getReason().getFirstSOAPText();
            if (soapText != null) {
                String xmlLangString = soapText.getLang();
                if (xmlLangString != null) {
                    return AxiomUtils.toLocale(xmlLangString);
                }

            }
        }
        return null;
    }

}
