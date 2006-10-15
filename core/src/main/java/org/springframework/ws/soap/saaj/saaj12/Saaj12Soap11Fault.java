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

package org.springframework.ws.soap.saaj.saaj12;

import java.util.Locale;
import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.springframework.util.Assert;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.saaj.SaajSoapFaultException;
import org.springframework.ws.soap.saaj.support.SaajUtils;
import org.springframework.ws.soap.soap11.Soap11Fault;

/**
 * Internal class that uses SAAJ 1.2 to implement the <code>Soap11Fault</code> interface.
 *
 * @author Arjen Poutsma
 */
class Saaj12Soap11Fault implements Soap11Fault {

    private final SOAPFault saajFault;

    Saaj12Soap11Fault(SOAPFault saajFault) {
        Assert.notNull(saajFault, "No saajFault given");
        this.saajFault = saajFault;
    }

    public QName getName() {
        return SaajUtils.toQName(saajFault.getElementName());
    }

    public Source getSource() {
        return new DOMSource(saajFault);
    }

    public QName getFaultCode() {
        return SaajUtils.toQName(saajFault.getFaultCodeAsName());
    }

    public String getFaultString() {
        return saajFault.getFaultString();
    }

    public String getFaultActorOrRole() {
        return saajFault.getFaultActor();
    }

    public void setFaultActorOrRole(String faultActor) {
        try {
            saajFault.setFaultActor(faultActor);
        }
        catch (SOAPException ex) {
            throw new SaajSoapFaultException(ex);
        }
    }

    public Locale getFaultStringLocale() {
        return saajFault.getFaultStringLocale();
    }

    public SoapFaultDetail getFaultDetail() {
        Detail saajDetail = saajFault.getDetail();
        return saajDetail != null ? new Saaj12SoapFaultDetail(saajDetail) : null;
    }

    public SoapFaultDetail addFaultDetail() {
        try {
            Detail saajDetail = saajFault.addDetail();
            return saajDetail != null ? new Saaj12SoapFaultDetail(saajDetail) : null;
        }
        catch (SOAPException ex) {
            throw new SaajSoapFaultException(ex);
        }
    }
}
