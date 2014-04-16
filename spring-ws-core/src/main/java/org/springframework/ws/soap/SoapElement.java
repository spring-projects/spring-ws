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
import javax.xml.transform.Source;

/**
 * The base interface for all elements that are contained in a SOAP message.
 *
 * @author Arjen Poutsma
 * @see SoapMessage
 * @since 1.0.0
 */
public interface SoapElement {

    /**
     * Returns the qualified name of this element.
     *
     * @return the qualified name of this element
     */
    QName getName();

    /**
     * Returns the {@code Source} of this element. This includes the element itself, i.e.
     * {@code SoapEnvelope.getSource()} will include the {@code Envelope} tag.
     *
     * @return the {@code Source} of this element
     */
    Source getSource();

    /**
     * Adds an attribute with the specified qualified name and value to this element.
     *
     * @param name  the qualified name of the attribute
     * @param value the value of the attribute
     */
    void addAttribute(QName name, String value);

    /**
     * Removes the attribute with the specified name.
     *
     * @param name the qualified name of the attribute to remove
     */
    void removeAttribute(QName name);

    /**
     * Returns the value of the attribute with the specified qualified name.
     *
     * @param name the qualified name
     * @return the value, or {@code null} if there is no such attribute
     */
    String getAttributeValue(QName name);

    /**
     * Returns an {@code Iterator} over all of the attributes in element as {@link QName qualified names}.
     *
     * @return an iterator over all the attribute names
     */
    Iterator<QName> getAllAttributes();

    /**
     * Adds a namespace declaration with the specified prefix and URI to this element.
     *
     * @param prefix       the namespace prefix. Can be empty or null to declare the default namespace
     * @param namespaceUri the namespace uri
     * @throws SoapElementException in case of errors
     */
    void addNamespaceDeclaration(String prefix, String namespaceUri);

}
