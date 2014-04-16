/*
 * Copyright 2005-2014 the original author or authors.
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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.RolePlayer;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axiom.soap.SOAPProcessingException;

import org.springframework.util.ObjectUtils;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapHeaderException;
import org.springframework.ws.soap.soap12.Soap12Header;
import org.springframework.xml.namespace.QNameUtils;

/**
 * Axiom-specific version of {@code org.springframework.ws.soap.Soap12Header}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
class AxiomSoap12Header extends AxiomSoapHeader implements Soap12Header {

    AxiomSoap12Header(SOAPHeader axiomHeader, SOAPFactory axiomFactory) {
        super(axiomHeader, axiomFactory);
    }

    @Override
    public SoapHeaderElement addNotUnderstoodHeaderElement(QName headerName) {
        try {
            SOAPHeaderBlock notUnderstood =
                    getAxiomHeader().addHeaderBlock("NotUnderstood", getAxiomHeader().getNamespace());
            OMNamespace headerNamespace =
                    notUnderstood.declareNamespace(headerName.getNamespaceURI(), QNameUtils.getPrefix(headerName));
            notUnderstood.addAttribute("qname", headerNamespace.getPrefix() + ":" + headerName.getLocalPart(), null);
            return new AxiomSoapHeaderElement(notUnderstood, getAxiomFactory());
        }
        catch (SOAPProcessingException ex) {
            throw new AxiomSoapHeaderException(ex);
        }
    }

    @Override
    public SoapHeaderElement addUpgradeHeaderElement(String[] supportedSoapUris) {
        try {
            SOAPHeaderBlock upgrade = getAxiomHeader().addHeaderBlock("Upgrade", getAxiomHeader().getNamespace());
            for (String supportedSoapUri : supportedSoapUris) {
                OMElement supportedEnvelope = getAxiomFactory()
                        .createOMElement("SupportedEnvelope", getAxiomHeader().getNamespace(), upgrade);
                OMNamespace namespace = supportedEnvelope.declareNamespace(supportedSoapUri, "");
                supportedEnvelope.addAttribute("qname", namespace.getPrefix() + ":Envelope", null);
            }
            return new AxiomSoapHeaderElement(upgrade, getAxiomFactory());
        }
        catch (OMException ex) {
            throw new AxiomSoapHeaderException(ex);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<SoapHeaderElement> examineHeaderElementsToProcess(final String[] roles, final boolean isUltimateDestination)
            throws SoapHeaderException {
        RolePlayer rolePlayer = null;
        if (!ObjectUtils.isEmpty(roles)) {
            rolePlayer = new RolePlayer() {

                public List<?> getRoles() {
                    return Arrays.asList(roles);
                }

                public boolean isUltimateDestination() {
                    return isUltimateDestination;
                }
            };
        }
        Iterator<SOAPHeaderBlock> result = (Iterator<SOAPHeaderBlock>)getAxiomHeader().getHeadersToProcess(rolePlayer);
        return new AxiomSoapHeaderElementIterator(result);
    }
}
