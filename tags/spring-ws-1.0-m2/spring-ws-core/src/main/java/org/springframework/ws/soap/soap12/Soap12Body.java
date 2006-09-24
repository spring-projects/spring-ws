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

import java.util.Locale;
import javax.xml.namespace.QName;

import org.springframework.ws.soap.SoapBody;

/**
 * Subinterface of <code>SoapBody</code> that exposes SOAP 1.2 functionality. Necessary because SOAP 1.1 differs from
 * SOAP 1.2 with respect to SOAP Faults.
 *
 * @author Arjen Poutsma
 */
public interface Soap12Body extends SoapBody {

    /**
     * Adds a <code>DataEncodingUnknown</code> fault to the body.
     * <p/>
     * Adding a fault removes the current content of the body.
     *
     * @param subcodes the optional fully qualified fault subcodes
     * @param reason   the fault reason
     * @param locale   the language of the fault reason
     * @return the created <code>SoapFault</code>
     */
    Soap12Fault addDataEncodingUnknownFault(QName[] subcodes, String reason, Locale locale);
}
