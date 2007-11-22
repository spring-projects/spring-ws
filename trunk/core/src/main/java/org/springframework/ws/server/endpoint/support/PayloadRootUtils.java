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

package org.springframework.ws.server.endpoint.support;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.springframework.xml.namespace.QNameUtils;
import org.springframework.xml.transform.StaxSource;

/**
 * Helper class for determining the root qualified name of a Web Service payload.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public abstract class PayloadRootUtils {

    private PayloadRootUtils() {

    }

    /**
     * Returns the root qualified name of the given source, transforming it if necessary.
     *
     * @param source             the source to get the root element from
     * @param transformerFactory a transformer factory, necessary if the given source is not a <code>DOMSource</code>
     * @return the root element, or <code>null</code> if <code>source</code> is <code>null</code>
     */
    public static QName getPayloadRootQName(Source source, TransformerFactory transformerFactory)
            throws TransformerException, XMLStreamException {
        if (source == null) {
            return null;
        }
        else if (source instanceof DOMSource) {
            DOMSource domSource = (DOMSource) source;
            Node node = domSource.getNode();
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                return QNameUtils.getQNameForNode(node);
            }
            else if (node.getNodeType() == Node.DOCUMENT_NODE) {
                Document document = (Document) node;
                return QNameUtils.getQNameForNode(document.getDocumentElement());
            }
        }
        else if (source instanceof StaxSource) {
            StaxSource staxSource = (StaxSource) source;
            if (staxSource.getXMLStreamReader() != null) {
                XMLStreamReader streamReader = staxSource.getXMLStreamReader();
                if (streamReader.getEventType() == XMLStreamConstants.START_DOCUMENT) {
                    streamReader.nextTag();
                }
                if (streamReader.getEventType() == XMLStreamConstants.START_ELEMENT ||
                        streamReader.getEventType() == XMLStreamConstants.END_ELEMENT) {
                    return streamReader.getName();
                }
            }
        }
        // we have no other option than to transform
        Transformer transformer = transformerFactory.newTransformer();
        DOMResult domResult = new DOMResult();
        transformer.transform(source, domResult);
        Document document = (Document) domResult.getNode();
        return QNameUtils.getQNameForNode(document.getDocumentElement());
    }


}
