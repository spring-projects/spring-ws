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

package org.springframework.ws.soap.addressing;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.springframework.util.StringUtils;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.soap11.Soap11Body;
import org.springframework.ws.soap.soap12.Soap12Body;
import org.springframework.ws.soap.soap12.Soap12Fault;
import org.springframework.xml.transform.TransformerObjectSupport;
import org.springframework.xml.xpath.XPathExpression;
import org.springframework.xml.xpath.XPathExpressionFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/** @author Arjen Poutsma */
class AddressingHelper extends TransformerObjectSupport {

    private final WsAddressingVersion version;

    private final XPathExpression toExpression;

    private XPathExpression actionExpression;

    private XPathExpression messageIdExpression;

    private XPathExpression fromExpression;

    private XPathExpression replyToExpression;

    private XPathExpression faultToExpression;

    private XPathExpression addressExpression;

    private XPathExpression referencePropertiesExpression;

    private XPathExpression referenceParametersExpression;

    public AddressingHelper(WsAddressingVersion version) {
        this.version = version;
        Properties namespaces = new Properties();
        namespaces.setProperty(version.getNamespacePrefix(), version.getNamespaceUri());
        toExpression = createNormalizedExpression(version.getToName(), namespaces);
        actionExpression = createNormalizedExpression(version.getActionName(), namespaces);
        messageIdExpression = createNormalizedExpression(version.getMessageIdName(), namespaces);
        fromExpression = createExpression(version.getFromName(), namespaces);
        replyToExpression = createExpression(version.getReplyToName(), namespaces);
        faultToExpression = createExpression(version.getFaultToName(), namespaces);
        addressExpression = createNormalizedExpression(version.getAddressName(), namespaces);
        if (version.getReferencePropertiesName() != null) {
            referencePropertiesExpression = createChildrenExpression(version.getReferencePropertiesName(), namespaces);
        }
        if (version.getReferenceParametersName() != null) {
            referenceParametersExpression = createChildrenExpression(version.getReferenceParametersName(), namespaces);
        }
    }

    private XPathExpression createExpression(QName name, Properties namespaces) {
        String expression = name.getPrefix() + ":" + name.getLocalPart();
        return XPathExpressionFactory.createXPathExpression(expression, namespaces);
    }

    private XPathExpression createNormalizedExpression(QName name, Properties namespaces) {
        String expression = "normalize-space(" + name.getPrefix() + ":" + name.getLocalPart() + ")";
        return XPathExpressionFactory.createXPathExpression(expression, namespaces);
    }

    private XPathExpression createChildrenExpression(QName name, Properties namespaces) {
        String expression = name.getPrefix() + ":" + name.getLocalPart() + "/*";
        return XPathExpressionFactory.createXPathExpression(expression, namespaces);
    }

    public MessageAddressingProperties getMessageAddressingProperties(SoapMessage message) throws TransformerException {
        Element headerElement = getSoapHeaderElement(message);
        String to = toExpression.evaluateAsString(headerElement);
        EndpointReference from = getEndpointReference(fromExpression.evaluateAsNode(headerElement));
        EndpointReference replyTo = getEndpointReference(replyToExpression.evaluateAsNode(headerElement));
        EndpointReference faultTo = getEndpointReference(faultToExpression.evaluateAsNode(headerElement));
        String action = actionExpression.evaluateAsString(headerElement);
        String messageId = messageIdExpression.evaluateAsString(headerElement);
        return new MessageAddressingProperties(to, from, replyTo, faultTo, action, messageId);
    }

    /** Given a ReplyTo, FaultTo, or From node, returns an endpoint reference. */
    private EndpointReference getEndpointReference(Node node) {
        if (node == null) {
            return null;
        }
        String address = addressExpression.evaluateAsString(node);
        if (!StringUtils.hasLength(address)) {
            return null;
        }
        List referenceProperties = referencePropertiesExpression != null ?
                referencePropertiesExpression.evaluateAsNodeList(node) : Collections.EMPTY_LIST;
        List referenceParameters = referenceParametersExpression != null ?
                referenceParametersExpression.evaluateAsNodeList(node) : Collections.EMPTY_LIST;
        return new EndpointReference(address, referenceProperties, referenceParameters);
    }

    public SoapFault addMessageHeaderRequiredFault(SoapMessage message) {
        return addAddressingFault(message, version.getMessageHeaderRequiredName(),
                version.getMessageHeaderRequiredText());
    }

    public SoapFault addDestinationUnreachableFault(SoapMessage message) {
        return addAddressingFault(message, version.getDestinationUnreachableName(),
                version.getDestinationUnreachableText());
    }

    public SoapFault addActionNotSupportedFault(SoapMessage message, String action) {
        return addAddressingFault(message, version.getActionNotSupportedName(),
                version.getActionNotSupportedText(action));
    }

    private SoapFault addAddressingFault(SoapMessage message, QName subcode, String reason) {
        if (message.getSoapBody() instanceof Soap11Body) {
            Soap11Body soapBody = (Soap11Body) message.getSoapBody();
            return soapBody.addFault(subcode, reason, Locale.ENGLISH);
        }
        else {
            Soap12Body soapBody = (Soap12Body) message.getSoapBody();
            Soap12Fault soapFault = (Soap12Fault) soapBody.addClientOrSenderFault(reason, Locale.ENGLISH);
            soapFault.addFaultSubcode(subcode);
            return soapFault;
        }
    }

    private Element getSoapHeaderElement(SoapMessage message) throws TransformerException {
        SoapHeader header = message.getSoapHeader();
        if (header.getSource() instanceof DOMSource) {
            DOMSource domSource = (DOMSource) header.getSource();
            if (domSource.getNode() != null && domSource.getNode().getNodeType() == Node.ELEMENT_NODE) {
                return (Element) domSource.getNode();
            }
        }
        Transformer transformer = createTransformer();
        DOMResult domResult = new DOMResult();
        transformer.transform(message.getSoapHeader().getSource(), domResult);
        Document document = (Document) domResult.getNode();
        return document.getDocumentElement();
    }

    public boolean hasNoneAddress(EndpointReference reference) {
        String none = version.getNoneUri();
        return none != null && none.equals(reference.getAddress());
    }

    public boolean hasAnonymousAddress(EndpointReference reference) {
        String anonymous = version.getAnonymousUri();
        return anonymous != null && anonymous.equals(reference.getAddress());
    }

    public void addAddressingHeaders(SoapMessage response, MessageAddressingProperties map)
            throws TransformerException {
        SoapHeader header = response.getSoapHeader();
        SoapHeaderElement messageId = header.addHeaderElement(version.getMessageIdName());
        messageId.setText(map.getMessageId());
        SoapHeaderElement relatesTo = header.addHeaderElement(version.getRelatesToName());
        relatesTo.setText(map.getRelatesTo());
        SoapHeaderElement to = header.addHeaderElement(version.getToName());
        to.setText(map.getTo());
        to.setMustUnderstand(true);
        Transformer transformer = createTransformer();
        for (Iterator iterator = map.getReferenceParameters().iterator(); iterator.hasNext();) {
            Node node = (Node) iterator.next();
            DOMSource source = new DOMSource(node);
            transformer.transform(source, header.getResult());
        }
        for (Iterator iterator = map.getReferenceProperties().iterator(); iterator.hasNext();) {
            Node node = (Node) iterator.next();
            DOMSource source = new DOMSource(node);
            transformer.transform(source, header.getResult());
        }
    }

    public boolean supports(SoapMessage message) {
        SoapHeader header = message.getSoapHeader();
        if (header != null) {
            for (Iterator iterator = header.examineAllHeaderElements(); iterator.hasNext();) {
                SoapHeaderElement headerElement = (SoapHeaderElement) iterator.next();
                if (version.getNamespaceUri().equals(headerElement.getName().getNamespaceURI())) {
                    return true;
                }
            }
        }
        return false;

    }

    public boolean understands(SoapHeaderElement header) {
        return version.getNamespaceUri().equals(header.getName().getNamespaceURI());
    }
}
