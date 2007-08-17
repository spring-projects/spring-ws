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

import java.util.Iterator;
import java.util.Locale;
import javax.xml.namespace.QName;

import org.springframework.ws.soap.SoapFault;

/**
 * Subinterface of <code>SoapFault</code> that exposes SOAP 1.2 functionality. Necessary because SOAP 1.1 differs from
 * SOAP 1.2 with respect to SOAP Faults.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public interface Soap12Fault extends SoapFault {

    /**
     * Returns an iteration over the fault subcodes. The subcodes are returned in order: from top to bottom.
     *
     * @return an Iterator that contains <code>QNames</code> representing the fault subcodes
     */
    Iterator getFaultSubcodes();

    /**
     * Adds a fault subcode this fault.
     *
     * @param subcode the qualified name of the subcode
     */
    void addFaultSubcode(QName subcode);

    /** Returns the fault node. Optional. */
    String getFaultNode();

    /** Sets the fault node. */
    void setFaultNode(String uri);

    /** Sets the specified fault reason text. */
    void setFaultReasonText(Locale locale, String text);

    /** Returns the reason associated with the given language. */
    String getFaultReasonText(Locale locale);

}
