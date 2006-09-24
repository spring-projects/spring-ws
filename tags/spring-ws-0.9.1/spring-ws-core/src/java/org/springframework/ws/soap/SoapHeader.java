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

package org.springframework.ws.soap;

import java.util.Iterator;
import javax.xml.namespace.QName;

/**
 * Represents the <code>Header</code> element in a SOAP message. A SOAP header contains <code>SoapHeaderElement</code>s,
 * which represent the individual headers.
 *
 * @author Arjen Poutsma
 * @see SoapHeaderElement
 * @see SoapEnvelope#getHeader()
 */
public interface SoapHeader extends SoapElement {

    /**
     * Adds a new <code>SoapHeaderElement</code> with the specified qualified name to this header.
     *
     * @param name the qualified name of the new header element
     * @return the created <code>SoapHeaderElement</code>
     */
    SoapHeaderElement addHeaderElement(QName name);

    /**
     * Returns an <code>Iterator</code> over all the <code>SoapHeaderElement</code>s that have the specified role and
     * that have a <code>MustUnderstand</code> attribute whose value is equivalent to <code>true</code>.
     *
     * @param role the role for which to search
     * @return an <code>Iterator</code> over all the <code>SoapHeaderElement</code>s that contain the specified role and
     *         are marked as <code>MustUnderstand</code>
     */
    Iterator examineMustUnderstandHeaderElements(String role);

}
