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

package org.springframework.ws.soap.saaj.support;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;

import org.springframework.util.StringUtils;

/**
 * Collection of generic utility methods to work with SAAJ. Includes conversion from <code>Name</code>s to
 * <code>QName</code>s and vice-versa.
 *
 * @author Arjen Poutsma
 * @see Name
 * @see QName
 */
public abstract class SaajUtils {

    /**
     * Converts a <code>javax.xml.namespace.QName</code> to a <code>javax.xml.soap.Name</code>. A
     * <code>SOAPEnvelope</code> is required to create the name. The supplied name must be fully qualified, i.e.
     * namespace, prefix, and local name must be specified.
     *
     * @param qName    the <code>QName</code> to convert
     * @param envelope a <code>SOAPEnvelope</code> necessary to create the <code>Name</code>
     * @return the converted SAAJ Name
     * @throws SOAPException            if conversion is unsuccessful
     * @throws IllegalArgumentException if <code>qName</code> is not fully qualified
     */
    public static Name toName(QName qName, SOAPEnvelope envelope) throws SOAPException {
        if (StringUtils.hasLength(qName.getNamespaceURI()) && StringUtils.hasLength(qName.getPrefix())) {
            return envelope.createName(qName.getLocalPart(), qName.getPrefix(), qName.getNamespaceURI());
        }
        else if (StringUtils.hasLength(qName.getNamespaceURI())) {
            throw new IllegalArgumentException("Supplied QName [" + qName + "] must have a prefix");
        }
        else {
            return envelope.createName(qName.getLocalPart());
        }
    }

    /**
     * Converts a <code>javax.xml.soap.Name</code> to a <code>javax.xml.namespace.QName</code>.
     *
     * @param name the <code>Name</code> to convert
     * @return the converted <code>QName</code>
     */
    public static QName toQName(Name name) {
        if (StringUtils.hasLength(name.getURI()) && StringUtils.hasLength(name.getPrefix())) {
            return new QName(name.getURI(), name.getLocalName(), name.getPrefix());
        }
        else if (StringUtils.hasLength(name.getURI())) {
            return new QName(name.getURI(), name.getLocalName());
        }
        else {
            return new QName(name.getLocalName());
        }
    }
}
