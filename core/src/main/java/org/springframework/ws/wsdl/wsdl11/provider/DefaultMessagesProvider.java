/*
 * Copyright 2008 the original author or authors.
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

package org.springframework.ws.wsdl.wsdl11.provider;

import java.util.Iterator;
import javax.wsdl.Definition;
import javax.wsdl.Message;
import javax.wsdl.Part;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.springframework.util.Assert;

/**
 * Default implementation of the {@link MessagesProvider}.
 * <p/>
 * Simply adds all elements contained in the schema(s) as messages.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public class DefaultMessagesProvider implements MessagesProvider {

    public void addMessages(Definition definition) throws WSDLException {
        Types types = definition.getTypes();
        Assert.notNull(types, "No types element present in definition");
        for (Iterator iterator = types.getExtensibilityElements().iterator(); iterator.hasNext();) {
            ExtensibilityElement extensibilityElement = (ExtensibilityElement) iterator.next();
            if (extensibilityElement instanceof Schema) {
                Schema schema = (Schema) extensibilityElement;
                if (schema.getElement() != null) {
                    createMessages(definition, schema.getElement());
                }
            }
        }
    }

    private void createMessages(Definition definition, Element schemaElement) throws WSDLException {
        String schemaTargetNamespace = schemaElement.getAttribute("targetNamespace");
        Assert.hasText("No targetNamespace defined on schema");
        NodeList children = schemaElement.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) child;
                if (isMessageElement(childElement)) {
                    QName elementName = new QName(schemaTargetNamespace, childElement.getAttribute("name"));
                    Message message = definition.createMessage();
                    populateMessage(definition, message, elementName);
                    Part part = definition.createPart();
                    populatePart(definition, part, elementName);
                    message.addPart(part);
                    message.setUndefined(false);
                    definition.addMessage(message);
                }
            }
        }
    }

    /**
     * Indicates whether the given element should be includes as {@link Message} in the definition.
     * <p/>
     * Default implementation checks whether the element has the XML Schema namespace, and if it has the local name
     * "element".
     *
     * @param element the element elligable for being a message
     * @return <code>true</code> if to be included as message; <code>false</code> otherwise
     */
    protected boolean isMessageElement(Element element) {
        return "element".equals(element.getLocalName()) &&
                "http://www.w3.org/2001/XMLSchema".equals(element.getNamespaceURI());
    }

    /**
     * Called after the {@link Message} has been created.
     * <p/>
     * Default implementation sets the name of the message to the element name.
     *
     * @param definition  the WSDL4J <code>Definition</code>
     * @param message     the WSDL4J <code>Message</code>
     * @param elementName the element name
     * @throws WSDLException in case of errors
     */
    protected void populateMessage(Definition definition, Message message, QName elementName) throws WSDLException {
        message.setQName(new QName(definition.getTargetNamespace(), elementName.getLocalPart()));
    }

    /**
     * Called after the {@link Part} has been created.
     * <p/>
     * Default implementation sets the element name of the part.
     *
     * @param definition  the WSDL4J <code>Definition</code>
     * @param part        the WSDL4J <code>Part</code>
     * @param elementName the elementName @throws WSDLException in case of errors
     * @see Part#setElementName(javax.xml.namespace.QName)
     */
    protected void populatePart(Definition definition, Part part, QName elementName) throws WSDLException {
        part.setElementName(elementName);
        part.setName(elementName.getLocalPart());
    }

}
