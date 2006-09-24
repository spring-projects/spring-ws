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
package org.springframework.oxm;

import java.io.IOException;

import javax.xml.transform.Result;

/**
 * Defines the contract for Object XML Mapping Marshallers. Implementations of this interface can serialize a given
 * Object to a XML Stream.
 * <p>
 * Although the <code>marshal</code> method accepts a <code>java.lang.Object</code> as its first parameter,
 * most <code>Marshaller</code> implementations cannot handle arbitrary <code>java.lang.Object</code>. Instead, a
 * object class must be registered with the marshaller, or have a common base class.
 * 
 * @author Arjen Poutsma
 */
public interface Marshaller {

    /**
     * Marshals the object graph with the given root into the provided <code>javax.xml.transform.Result</code>.
     * 
     * @param graph the root of the object graph to marshal
     * @param result the result to marshal to
     * @throws XmlMappingException if the given object cannot be marshalled to the result
     * @throws IOException if an I/O exception occurs
     */
    void marshal(Object graph, Result result) throws XmlMappingException, IOException;

}
