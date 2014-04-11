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

package org.springframework.ws.soap.saaj;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapHeaderException;
import org.springframework.ws.soap.soap12.Soap12Header;

/**
 * SAAJ-specific implementation of the <code>Soap12Header</code> interface. Wraps a {@link javax.xml.soap.SOAPHeader}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
class SaajSoap12Header extends SaajSoapHeader implements Soap12Header {

    SaajSoap12Header(SOAPHeader header) {
        super(header);
    }

    public SoapHeaderElement addNotUnderstoodHeaderElement(QName headerName) {
        try {
	        SOAPHeaderElement headerElement =
			        getSaajHeader().addNotUnderstoodHeaderElement(headerName);
            return new SaajSoapHeaderElement(headerElement);
        }
        catch (SOAPException ex) {
            throw new SaajSoapHeaderException(ex);
        }
    }

    public SoapHeaderElement addUpgradeHeaderElement(String[] supportedSoapUris) {
        try {
	        SOAPHeaderElement headerElement =
			        getSaajHeader().addUpgradeHeaderElement(supportedSoapUris);
            return new SaajSoapHeaderElement(headerElement);
        }
        catch (SOAPException ex) {
            throw new SaajSoapHeaderException(ex);
        }
    }

    public Iterator<SoapHeaderElement> examineHeaderElementsToProcess(String[] roles, boolean isUltimateDestination)
            throws SoapHeaderException {
        List<SOAPHeaderElement> result = new ArrayList<SOAPHeaderElement>();
	    Iterator<SOAPHeaderElement> iterator = getSaajHeader().examineAllHeaderElements();
        while (iterator.hasNext()) {
            SOAPHeaderElement saajHeaderElement = iterator.next();
            String headerRole = saajHeaderElement.getRole();
            if (shouldProcess(headerRole, roles, isUltimateDestination)) {
                result.add(saajHeaderElement);
            }
        }
        return new SaajSoapHeaderElementIterator(result.iterator());

    }

    private boolean shouldProcess(String headerRole, String[] roles, boolean isUltimateDestination) {
        if (!StringUtils.hasLength(headerRole)) {
            return true;
        }
        if (SOAPConstants.URI_SOAP_1_2_ROLE_NEXT.equals(headerRole)) {
            return true;
        }
        if (SOAPConstants.URI_SOAP_1_2_ROLE_ULTIMATE_RECEIVER.equals(headerRole)) {
            return isUltimateDestination;
        }
        if (SOAPConstants.URI_SOAP_1_2_ROLE_NONE.equals(headerRole)) {
            return false;
        }
        if (!ObjectUtils.isEmpty(roles)) {
            for (String role : roles) {
                if (role.equals(headerRole)) {
                    return true;
                }
            }
        }
        return false;
    }
}
