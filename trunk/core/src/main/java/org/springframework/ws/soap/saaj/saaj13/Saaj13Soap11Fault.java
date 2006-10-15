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

package org.springframework.ws.soap.saaj.saaj13;

import java.util.Locale;
import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;

import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.saaj.SaajSoapFaultException;
import org.springframework.ws.soap.soap11.Soap11Fault;

/**
 * Internal class that uses SAAJ 1.3 to implement the <code>Soap11Fault</code> interface.
 *
 * @author Arjen Poutsma
 */
class Saaj13Soap11Fault extends Saaj13SoapElement implements Soap11Fault {

    Saaj13Soap11Fault(SOAPFault saajFault) {
        super(saajFault);
    }

    public String getFaultString() {
        return getSaajFault().getFaultString();
    }

    public Locale getFaultStringLocale() {
        return getSaajFault().getFaultStringLocale();
    }

    public SoapFaultDetail addFaultDetail() {
        try {
            Detail saajDetail = getSaajFault().addDetail();
            return saajDetail != null ? new Saaj13SoapFaultDetail(saajDetail) : null;
        }
        catch (SOAPException ex) {
            throw new SaajSoapFaultException(ex);
        }
    }

    public String getFaultActorOrRole() {
        return getSaajFault().getFaultActor();
    }

    public QName getFaultCode() {
        return getSaajFault().getFaultCodeAsQName();
    }

    public SoapFaultDetail getFaultDetail() {
        Detail saajDetail = getSaajFault().getDetail();
        return saajDetail != null ? new Saaj13SoapFaultDetail(saajDetail) : null;
    }

    public void setFaultActorOrRole(String faultActor) {
        try {
            getSaajFault().setFaultActor(faultActor);
        }
        catch (SOAPException ex) {
            throw new SaajSoapFaultException(ex);
        }
    }

    private SOAPFault getSaajFault() {
        return (SOAPFault) getSaajElement();
    }
}
