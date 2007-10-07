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
import org.springframework.xml.xpath.XPathExpression;
import org.springframework.xml.xpath.XPathExpressionFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Abstract extension of the {@link AbstractWsAddressingInterceptor} that uses a {@link WsAddressingVersion}.
 *
 * @author Arjen Poutsma
 * @since 1.1.0
 */
abstract class AbstractVersionBasedWsAddressingInterceptor extends AbstractWsAddressingInterceptor {

    private final WsAddressingVersion version;

    private final XPathExpression toExpression;

    private final XPathExpression actionExpression;

    private final XPathExpression messageIdExpression;

    private final XPathExpression fromExpression;

    private final XPathExpression replyToExpression;

    private final XPathExpression faultToExpression;

    private final XPathExpression addressExpression;

    private XPathExpression referencePropertiesExpression;

    private XPathExpression referenceParametersExpression;

    /**
     * Creates a new instance of the {@link AbstractVersionBasedWsAddressingInterceptor} with the given {@link
     * WsAddressingVersion}.
     */
    protected AbstractVersionBasedWsAddressingInterceptor(WsAddressingVersion version) {
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

    public boolean understands(SoapHeaderElement header) {
        return version.getNamespaceUri().equals(header.getName().getNamespaceURI());
    }

    protected void addAddressingHeaders(SoapMessage message, MessageAddressingProperties map)
            throws TransformerException {
        SoapHeader header = message.getSoapHeader();
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

    /**
     * Adds a Message Addressing Header Required fault to the given message.
     *
     * @see <a href="http://www.w3.org/TR/ws-addr-soap/#missingmapfault">Message Addressing Header Required</a>
     */
    protected SoapFault addMessageAddressingHeaderRequiredFault(SoapMessage message) {
        return addAddressingFault(message, version.getMessageAddressingHeaderRequiredFaultSubcode(),
                version.getMessageAddressingHeaderRequiredFaultReason());
    }

    private SoapFault addAddressingFault(SoapMessage message, QName subcode, String reason) {
        if (message.getSoapBody() instanceof Soap11Body) {
            Soap11Body soapBody = (Soap11Body) message.getSoapBody();
            return soapBody.addFault(subcode, reason, Locale.ENGLISH);
        }
        else if (message.getSoapBody() instanceof Soap12Body) {
            Soap12Body soapBody = (Soap12Body) message.getSoapBody();
            Soap12Fault soapFault = (Soap12Fault) soapBody.addClientOrSenderFault(reason, Locale.ENGLISH);
            soapFault.addFaultSubcode(subcode);
            return soapFault;
        }
        return null;
    }

    /**
     * Returns the {@link MessageAddressingProperties} for the given message.
     *
     * @param message the message to find the map for
     * @return the message addressing properties
     */
    protected MessageAddressingProperties getMessageAddressingProperties(SoapMessage message)
            throws TransformerException {
        Element headerElement = getSoapHeaderElement(message);
        String to = toExpression.evaluateAsString(headerElement);
        EndpointReference from = getEndpointReference(fromExpression.evaluateAsNode(headerElement));
        EndpointReference replyTo = getEndpointReference(replyToExpression.evaluateAsNode(headerElement));
        EndpointReference faultTo = getEndpointReference(faultToExpression.evaluateAsNode(headerElement));
        String action = actionExpression.evaluateAsString(headerElement);
        String messageId = messageIdExpression.evaluateAsString(headerElement);
        return new MessageAddressingProperties(to, from, replyTo, faultTo, action, messageId);
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

    /**
     * Indicates whether the given endpoint reference has a Anonymous address. This address is used to indicate that a
     * message should be sent in-band.
     *
     * @see <a href="http://www.w3.org/TR/ws-addr-core/#formreplymsg">Formulating a Reply Message</a>
     */
    protected boolean hasAnonymousAddress(EndpointReference epr) {
        String anonymous = version.getAnonymousUri();
        return anonymous != null && anonymous.equals(epr.getAddress());
    }

    /**
     * Indicates whether the given endpoint reference has a None address. Messages to be sent to this address will not
     * be sent.
     *
     * @see <a href="http://www.w3.org/TR/ws-addr-core/#sendmsgepr">Sending a Message to an EPR</a>
     */
    protected boolean hasNoneAddress(EndpointReference epr) {
        String none = version.getNoneUri();
        return none != null && none.equals(epr.getAddress());
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
}