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

import javax.xml.transform.Result;

/**
 * Represents the contents of an individual SOAP header in the a SOAP message. All <code>SoapHeaderElement</code>s are
 * contained in a <code>SoapHeader</code>.
 *
 * @author Arjen Poutsma
 * @see SoapHeader
 * @since 1.0
 */
public interface SoapHeaderElement extends SoapElement {

    /**
     * Returns the actor or role for this header element. In a SOAP 1.1 compliant message, this will read the
     * <code>actor</code> attribute; in SOAP 1.2, the <code>role</code> attribute.
     *
     * @return the role of the header
     */
    String getActorOrRole() throws SoapHeaderException;

    /**
     * Sets the actor or role for this header element. In a SOAP 1.1 compliant message, this will result in an
     * <code>actor</code> attribute being set; in SOAP 1.2, a <code>actorOrRole</code> attribute.
     *
     * @param actorOrRole the actorOrRole value
     */
    void setActorOrRole(String actorOrRole) throws SoapHeaderException;

    /**
     * Indicates whether the <code>mustUnderstand</code> attribute for this header element is set.
     *
     * @return <code>true</code> if the <code>mustUnderstand</code> attribute is set; <code>false</code> otherwise
     */
    boolean getMustUnderstand() throws SoapHeaderException;

    /**
     * Sets the <code>mustUnderstand</code> attribute for this header element. If the attribute is on, the role who
     * receives the header must process it.
     *
     * @param mustUnderstand <code>true</code> to set the <code>mustUnderstand</code> attribute on; <code>false</code>
     *                       to turn it off
     */
    void setMustUnderstand(boolean mustUnderstand) throws SoapHeaderException;

    /** Returns a <code>Result</code> that allows for writing to the <strong>contents</strong> of the header element. */
    Result getResult() throws SoapHeaderException;

    /**
     * Returns the text content of this header element, if any.
     *
     * @return the text content of this header element
     */
    String getText();

    /**
     * Sets the text content of this header element.
     *
     * @param content the new text content of this header element
     */
    void setText(String content);
}
