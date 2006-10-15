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

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;

import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.saaj.SaajSoapHeaderException;
import org.springframework.ws.soap.soap12.Soap12Header;

/**
 * Internal class that uses SAAJ 1.3 to implement the <code>Soap12Header</code> interface.
 *
 * @author Arjen Poutsma
 */
class Saaj13Soap12Header extends Saaj13SoapHeader implements Soap12Header {

    Saaj13Soap12Header(SOAPHeader saajHeader) {
        super(saajHeader);
    }

    public SoapHeaderElement addNotUnderstoodHeaderElement(QName headerName) {
        try {
            SOAPHeaderElement saajHeaderElement = saajHeader.addNotUnderstoodHeaderElement(headerName);
            return new Saaj13SoapHeaderElement(saajHeaderElement);
        }
        catch (SOAPException ex) {
            throw new SaajSoapHeaderException(ex);
        }
    }

    public SoapHeaderElement addUpgradeHeaderElement(String[] supportedSoapUris) {
        try {
            SOAPHeaderElement saajHeaderElement = saajHeader.addUpgradeHeaderElement(supportedSoapUris);
            return new Saaj13SoapHeaderElement(saajHeaderElement);
        }
        catch (SOAPException ex) {
            throw new SaajSoapHeaderException(ex);
        }
    }
}
