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

import javax.xml.transform.Result;
import javax.xml.transform.sax.SAXResult;

import org.apache.axiom.om.OMException;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.springframework.ws.soap.SoapHeaderElement;

/** Axiom-specific version of <code>org.springframework.ws.soap.SoapHeaderHeaderElement</code>. */
class AxiomSoapHeaderElement extends AxiomSoapElement implements SoapHeaderElement {

    public AxiomSoapHeaderElement(SOAPHeaderBlock axiomHeaderBlock, SOAPFactory axiomFactory) {
        super(axiomHeaderBlock, axiomFactory);
    }

    public String getActorOrRole() {
        return getAxiomHeaderBlock().getRole();
    }

    public void setActorOrRole(String role) {
        getAxiomHeaderBlock().setRole(role);
    }

    public boolean getMustUnderstand() {
        return getAxiomHeaderBlock().getMustUnderstand();
    }

    public void setMustUnderstand(boolean mustUnderstand) {
        getAxiomHeaderBlock().setMustUnderstand(mustUnderstand);
    }

    public Result getResult() {
        try {
            return new SAXResult(new AxiomContentHandler(getAxiomHeaderBlock()));
        }
        catch (OMException ex) {
            throw new AxiomSoapHeaderException(ex);
        }

    }

    protected SOAPHeaderBlock getAxiomHeaderBlock() {
        return (SOAPHeaderBlock) getAxiomElement();
    }

}
