/*
 * Copyright 2007 the original author or authors.
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

package org.springframework.xml.dom;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Convenient utility methods for dealing with DOM.
 *
 * @author Arjen Poutsma
 */
public abstract class DomUtils {

    /**
     * Returns the root element of the given source, transforming it if necessary.
     *
     * @param source      the source to get the root element from
     * @param transformer a transformer
     * @return the root element
     */
    public static Element getRootElement(Source source, Transformer transformer) throws TransformerException {
        if (source instanceof DOMSource) {
            DOMSource domSource = (DOMSource) source;
            Node node = domSource.getNode();
            if (node == null) {
                return null;
            }
            else if (node.getNodeType() == Node.ELEMENT_NODE) {
                return (Element) node;
            }
            else if (node.getNodeType() == Node.DOCUMENT_NODE) {
                Document document = (Document) node;
                return document.getDocumentElement();
            }
        }
        DOMResult domResult = new DOMResult();
        transformer.transform(source, domResult);
        Document document = (Document) domResult.getNode();
        return document.getDocumentElement();
    }


}
