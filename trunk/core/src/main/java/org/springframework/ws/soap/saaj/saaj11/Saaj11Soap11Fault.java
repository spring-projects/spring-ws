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

package org.springframework.ws.soap.saaj.saaj11;

import java.util.Locale;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPFault;
import javax.xml.transform.Source;

import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.soap11.Soap11Fault;

/**
 * @author Arjen Poutsma
 */
public class Saaj11Soap11Fault implements Soap11Fault {

    public Saaj11Soap11Fault(SOAPFault saajFault) {
    }

    public String getFaultString() {
        //TODO implement
        throw new UnsupportedOperationException("Not implemented");
    }

    public Locale getFaultStringLocale() {
        //TODO implement
        throw new UnsupportedOperationException("Not implemented");
    }

    public QName getFaultCode() {
        //TODO implement
        throw new UnsupportedOperationException("Not implemented");
    }

    public String getFaultActorOrRole() {
        //TODO implement
        throw new UnsupportedOperationException("Not implemented");
    }

    public void setFaultActorOrRole(String faultActor) {
        //TODO implement
        throw new UnsupportedOperationException("Not implemented");
    }

    public SoapFaultDetail getFaultDetail() {
        //TODO implement
        throw new UnsupportedOperationException("Not implemented");
    }

    public SoapFaultDetail addFaultDetail() {
        //TODO implement
        throw new UnsupportedOperationException("Not implemented");
    }

    public QName getName() {
        //TODO implement
        throw new UnsupportedOperationException("Not implemented");
    }

    public Source getSource() {
        //TODO implement
        throw new UnsupportedOperationException("Not implemented");
    }
}
