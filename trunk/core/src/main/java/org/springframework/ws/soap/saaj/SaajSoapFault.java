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
 *  * limitations under the License.
 */

package org.springframework.ws.soap.saaj;

import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;

import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapFaultDetail;

abstract class SaajSoapFault extends SaajSoapElement implements SoapFault {

    protected SaajSoapFault(SOAPFault fault, SaajImplementationStrategy strategy) {
        super(fault, strategy);
    }

    public QName getFaultCode() {
        return getStrategy().getFaultCode(getSaajFault());
    }

    public String getFaultActorOrRole() {
        return getSaajFault().getFaultActor();
    }

    public void setFaultActorOrRole(String faultActor) {
        try {
            getSaajFault().setFaultActor(faultActor);
        }
        catch (SOAPException ex) {
            throw new SaajSoapFaultException(ex);
        }
    }

    public SoapFaultDetail getFaultDetail() {
        Detail saajDetail = getSaajFault().getDetail();
        return saajDetail == null ? null : new SaajSoapFaultDetail(saajDetail);
    }

    public SoapFaultDetail addFaultDetail() {
        try {
            Detail saajDetail = getSaajFault().addDetail();
            return new SaajSoapFaultDetail(saajDetail);
        }
        catch (SOAPException ex) {
            throw new SaajSoapFaultException(ex);
        }

    }

    protected final SOAPFault getSaajFault() {
        return (SOAPFault) getSaajElement();
    }

}
