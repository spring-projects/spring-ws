/*
 * Copyright 2005 the original author or authors.
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

import javax.xml.namespace.QName;

import org.springframework.ws.WebServiceMessage;
import org.w3c.dom.Element;

/**
 * @author Arjen Poutsma
 */
public interface SoapMessage extends WebServiceMessage {

    /**
     * Returns the SOAP Header of this message. This is the parent of the header elements.
     *
     * @return the SOAP header element
     * @see #getHeaderElements()
     */
    Element getHeader();

    /**
     * Returns a specific SOAP header element in this message.
     *
     * @param qName the qualified name of the header element
     * @return the SOAP header element
     */
    Element[] getHeaderElements(QName qName);

    /**
     * Returns all SOAP headers elements in this message.
     *
     * @return the SOAP header elements
     */
    Element[] getHeaderElements();

    /**
     * Returns all SOAP headers elements that have the specified actor role and that have a <code>MustUnderstand</code>
     * attribute whose value is equivalent to <code>true</code>.
     *
     * @param actor the actor for which to search, or <code>null</code> to return all headers
     * @return all the header elements that contain the specified actor and are marked as <code>MustUnderstand</code>
     */
    Element[] getMustUnderstandHeaderElements(String actor);

    /**
     * Returns the SOAP fault element in the body of this message. Returns <code>null</code> if no fault is present.
     *
     * @return the SOAP fault element; or <code>null</code> if none exists
     */
    Element getFault();

    /**
     * Get the SOAP Action for this messaage, or <code>null</code> if not present.
     *
     * @return the SOAP Action.
     */
    String getSoapAction();

    /**
     * Creates a new SOAP Fault to the body using the supplied parameters and adds it to the body.
     *
     * @param faultCode   the qualified fault code
     * @param faultString an explanation of the fault
     * @param faultActor  the fault actor, which may be <code>null</code>
     * @return the SOAP Fault element
     */
    Element addFault(QName faultCode, String faultString, String faultActor);

    /**
     * Creates a new SOAP Header element using the supplied qualified name and other parameters. Apart from the
     * qualified name and <code>mustUnderstand</code> flag, all parameters are optional, and can be set to
     * <code>null</code>.
     *
     * @param qName          the qualified name of the header
     * @param mustUnderstand whether the SOAP mustUnderstand attribute for the created header is turned on
     * @param actor          the uri of the actor associated with the created header, or <code>null</code> for no actor
     * @return the created element
     */
    Element addHeaderElement(QName qName, boolean mustUnderstand, String actor);
}
