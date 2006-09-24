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

package org.springframework.ws.soap.soap12;

import javax.xml.namespace.QName;

import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;

/**
 * Subinterface of <code>SoapHeader</code> that exposes SOAP 1.2 functionality.
 *
 * @author Arjen Poutsma
 */
public interface Soap12Header extends SoapHeader {

    /**
     * Adds a new NotUnderstood <code>SoapHeaderElement</code> this header.
     *
     * @param headerName the qualified name of the header that was not understood
     * @return the created <code>SoapHeaderElement</code>
     * @throws org.springframework.ws.soap.SoapHeaderException
     *          if the header cannot be created
     */
    SoapHeaderElement addNotUnderstoodHeaderElement(QName headerName);

    /**
     * Adds a new Upgrade <code>SoapHeaderElement</code> this header.
     *
     * @param supportedSoapUris an array of the URIs of SOAP versions supported
     * @return the created <code>SoapHeaderElement</code>
     * @throws org.springframework.ws.soap.SoapHeaderException
     *          if the header cannot be created
     */
    SoapHeaderElement addUpgradeHeaderElement(java.lang.String[] supportedSoapUris);

}
