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

package org.springframework.ws.soap;

import java.util.Iterator;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;

/**
 * Represents the <code>Header</code> element in a SOAP message. A SOAP header contains <code>SoapHeaderElement</code>s,
 * which represent the individual headers.
 *
 * @author Arjen Poutsma
 * @see SoapHeaderElement
 * @see SoapEnvelope#getHeader()
 * @since 1.0.0
 */
public interface SoapHeader extends SoapElement {

    /**
     * Returns a <code>Result</code> that represents the concents of the header.
     * <p/>
     * The result can be used for marshalling.
     *
     * @return the <code>Result</code> of this element
     */
    Result getResult();

    /**
     * Adds a new <code>SoapHeaderElement</code> with the specified qualified name to this header.
     *
     * @param name the qualified name of the new header element
     * @return the created <code>SoapHeaderElement</code>
     * @throws SoapHeaderException if the header cannot be created
     */
    SoapHeaderElement addHeaderElement(QName name) throws SoapHeaderException;

    /**
     * Removes the <code>SoapHeaderElement</code> with the specified qualified name from this header.
     * <p/>
     * This method will only remove the first child element with the specified name. If no element is found with the
     * specified name, this method has no effect.
     *
     * @param name the qualified name of the header element to be removed
     * @throws SoapHeaderException if the header cannot be removed
     */
    void removeHeaderElement(QName name) throws SoapHeaderException;

    /**
     * Returns an <code>Iterator</code> over all the <code>SoapHeaderElement</code>s that have the specified actor or
     * role and that have a <code>MustUnderstand</code> attribute whose value is equivalent to <code>true</code>.
     *
     * @param actorOrRole the actor (SOAP 1.1) or role (SOAP 1.2) for which to search
     * @return an iterator over all the header elements that contain the specified actor/role and are marked as
     *         <code>MustUnderstand</code>
     * @throws SoapHeaderException if the headers cannot be returned
     * @see SoapHeaderElement
     */
    Iterator<SoapHeaderElement> examineMustUnderstandHeaderElements(String actorOrRole) throws SoapHeaderException;

    /**
     * Returns an <code>Iterator</code> over all the <code>SoapHeaderElement</code>s in this header.
     *
     * @return an iterator over all the header elements
     * @throws SoapHeaderException if the header cannot be returned
     * @see SoapHeaderElement
     */
    Iterator<SoapHeaderElement> examineAllHeaderElements() throws SoapHeaderException;

}
