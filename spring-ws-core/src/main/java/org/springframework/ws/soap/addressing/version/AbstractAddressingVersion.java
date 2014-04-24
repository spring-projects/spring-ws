/*
 * Copyright 2005-2014 the original author or authors.
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

package org.springframework.ws.soap.addressing.version;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.springframework.util.StringUtils;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.addressing.AddressingException;
import org.springframework.ws.soap.addressing.core.EndpointReference;
import org.springframework.ws.soap.addressing.core.MessageAddressingProperties;
import org.springframework.ws.soap.soap11.Soap11Body;
import org.springframework.ws.soap.soap12.Soap12Body;
import org.springframework.ws.soap.soap12.Soap12Fault;
import org.springframework.xml.namespace.QNameUtils;
import org.springframework.xml.transform.TransformerObjectSupport;
import org.springframework.xml.xpath.XPathExpression;
import org.springframework.xml.xpath.XPathExpressionFactory;

/**
 * Abstract base class for {@link AddressingVersion} implementations. Uses {@link XPathExpression}s to retrieve
 * addressing information.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public abstract class AbstractAddressingVersion extends TransformerObjectSupport implements AddressingVersion {

    private static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    private final XPathExpression toExpression;

    private final XPathExpression actionExpression;

    private final XPathExpression messageIdExpression;

    private final XPathExpression fromExpression;

    private final XPathExpression replyToExpression;

    private final XPathExpression faultToExpression;

    private final XPathExpression addressExpression;

    private final XPathExpression referencePropertiesExpression;

    private final XPathExpression referenceParametersExpression;

    protected AbstractAddressingVersion() {
        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put(getNamespacePrefix(), getNamespaceUri());
        toExpression = createNormalizedExpression(getToName(), namespaces);
        actionExpression = createNormalizedExpression(getActionName(), namespaces);
        messageIdExpression = createNormalizedExpression(getMessageIdName(), namespaces);
        fromExpression = createExpression(getFromName(), namespaces);
        replyToExpression = createExpression(getReplyToName(), namespaces);
        faultToExpression = createExpression(getFaultToName(), namespaces);
        addressExpression = createNormalizedExpression(getAddressName(), namespaces);
        if (getReferencePropertiesName() != null) {
            referencePropertiesExpression = createChildrenExpression(getReferencePropertiesName(), namespaces);
        }
        else {
            referencePropertiesExpression = null;
        }
        if (getReferenceParametersName() != null) {
            referenceParametersExpression = createChildrenExpression(getReferenceParametersName(), namespaces);
        }
        else {
            referenceParametersExpression = null;
        }
    }

    private XPathExpression createExpression(QName name, Map<String, String> namespaces) {
        String expression = name.getPrefix() + ":" + name.getLocalPart();
        return XPathExpressionFactory.createXPathExpression(expression, namespaces);
    }

    private XPathExpression createNormalizedExpression(QName name, Map<String, String> namespaces) {
        String expression = "normalize-space(" + name.getPrefix() + ":" + name.getLocalPart() + ")";
        return XPathExpressionFactory.createXPathExpression(expression, namespaces);
    }

    private XPathExpression createChildrenExpression(QName name, Map<String, String> namespaces) {
        String expression = name.getPrefix() + ":" + name.getLocalPart() + "/*";
        return XPathExpressionFactory.createXPathExpression(expression, namespaces);
    }

    @Override
    public MessageAddressingProperties getMessageAddressingProperties(SoapMessage message) {
        Element headerElement = getSoapHeaderElement(message);
        URI to = getUri(headerElement, toExpression);
        if (to == null) {
            to = getDefaultTo();
        }
        EndpointReference from = getEndpointReference(fromExpression.evaluateAsNode(headerElement));
        EndpointReference replyTo = getEndpointReference(replyToExpression.evaluateAsNode(headerElement));
        if (replyTo == null) {
            replyTo = getDefaultReplyTo(from);
        }
        EndpointReference faultTo = getEndpointReference(faultToExpression.evaluateAsNode(headerElement));
        if (faultTo == null) {
            faultTo = replyTo;
        }
        URI action = getUri(headerElement, actionExpression);
        URI messageId = getUri(headerElement, messageIdExpression);
        return new MessageAddressingProperties(to, from, replyTo, faultTo, action, messageId);
    }

    private URI getUri(Node node, XPathExpression expression) {
        String messageId = expression.evaluateAsString(node);
        if (!StringUtils.hasLength(messageId)) {
            return null;
        }
        try {
            return new URI(messageId);
        }
        catch (URISyntaxException e) {
            return null;
        }
    }

    private Element getSoapHeaderElement(SoapMessage message) {
        Source source = message.getSoapHeader().getSource();
        if (source instanceof DOMSource) {
            DOMSource domSource = (DOMSource) source;
            if (domSource.getNode() != null && domSource.getNode().getNodeType() == Node.ELEMENT_NODE) {
                return (Element) domSource.getNode();
            }
        }
        try {
            DOMResult domResult = new DOMResult();
            transform(source, domResult);
            Document document = (Document) domResult.getNode();
            return document.getDocumentElement();
        }
        catch (TransformerException ex) {
            throw new AddressingException("Could not transform SoapHeader to Document", ex);
        }
    }

    /** Given a ReplyTo, FaultTo, or From node, returns an endpoint reference. */
    private EndpointReference getEndpointReference(Node node) {
        if (node == null) {
            return null;
        }
        URI address = getUri(node, addressExpression);
        if (address == null) {
            return null;
        }
        List<Node> referenceProperties =
                referencePropertiesExpression != null ? referencePropertiesExpression.evaluateAsNodeList(node) :
                        Collections.<Node>emptyList();
        List<Node> referenceParameters =
                referenceParametersExpression != null ? referenceParametersExpression.evaluateAsNodeList(node) :
                        Collections.<Node>emptyList();
        return new EndpointReference(address, referenceProperties, referenceParameters);
    }

    @Override
    public void addAddressingHeaders(SoapMessage message, MessageAddressingProperties map) {
        SoapHeader header = message.getSoapHeader();
        header.addNamespaceDeclaration(getNamespacePrefix(), getNamespaceUri());
        // To
        if (map.getTo() != null) {
            SoapHeaderElement to = header.addHeaderElement(getToName());
            to.setText(map.getTo().toString());
            to.setMustUnderstand(true);
        }
        // From
        if (map.getFrom() != null) {
            SoapHeaderElement from = header.addHeaderElement(getFromName());
            addEndpointReference(from, map.getFrom());
        }
        //ReplyTo
        if (map.getReplyTo() != null) {
            SoapHeaderElement replyTo = header.addHeaderElement(getReplyToName());
            addEndpointReference(replyTo, map.getReplyTo());
        }
        // FaultTo
        if (map.getFaultTo() != null) {
            SoapHeaderElement faultTo = header.addHeaderElement(getFaultToName());
            addEndpointReference(faultTo, map.getFaultTo());
        }
        // Action
        SoapHeaderElement action = header.addHeaderElement(getActionName());
        action.setText(map.getAction().toString());
        // MessageID
        if (map.getMessageId() != null) {
            SoapHeaderElement messageId = header.addHeaderElement(getMessageIdName());
            messageId.setText(map.getMessageId().toString());
        }
        // RelatesTo
        if (map.getRelatesTo() != null) {
            SoapHeaderElement relatesTo = header.addHeaderElement(getRelatesToName());
            relatesTo.setText(map.getRelatesTo().toString());
        }
        addReferenceNodes(header.getResult(), map.getReferenceParameters());
        addReferenceNodes(header.getResult(), map.getReferenceProperties());
    }

    @Override
    public final boolean understands(SoapHeaderElement headerElement) {
        return getNamespaceUri().equals(headerElement.getName().getNamespaceURI());
    }

    /** Adds ReplyTo, FaultTo, or From EPR to the given header Element. */
    protected void addEndpointReference(SoapHeaderElement headerElement, EndpointReference epr) {
        if (epr == null || epr.getAddress() == null) {
            return;
        }
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();
            Element address = document.createElementNS(getNamespaceUri(), QNameUtils.toQualifiedName(getAddressName()));
            address.setTextContent(epr.getAddress().toString());
            transform(new DOMSource(address), headerElement.getResult());
            if (getReferenceParametersName() != null && !epr.getReferenceParameters().isEmpty()) {
                Element referenceParams = document.createElementNS(getNamespaceUri(),
                        QNameUtils.toQualifiedName(getReferenceParametersName()));
                addReferenceNodes(new DOMResult(referenceParams), epr.getReferenceParameters());
                transform(new DOMSource(referenceParams), headerElement.getResult());
            }
            if (getReferencePropertiesName() != null && !epr.getReferenceProperties().isEmpty()) {
                Element referenceProps = document.createElementNS(getNamespaceUri(),
                        QNameUtils.toQualifiedName(getReferencePropertiesName()));
                addReferenceNodes(new DOMResult(referenceProps), epr.getReferenceProperties());
                transform(new DOMSource(referenceProps), headerElement.getResult());
            }
        }
        catch (ParserConfigurationException ex) {
            throw new AddressingException("Could not add Endpoint Reference [" + epr + "] to header element", ex);
        }
        catch (TransformerException ex) {
            throw new AddressingException("Could not add reference properties/parameters to message", ex);
        }
    }

    protected void addReferenceNodes(Result result, List<Node> nodes) {
        try {
            for (Node node : nodes) {
                DOMSource source = new DOMSource(node);
                transform(source, result);
            }
        }
        catch (TransformerException ex) {
            throw new AddressingException("Could not add reference properties/parameters to message", ex);
        }
    }

    @Override
    public final SoapFault addInvalidAddressingHeaderFault(SoapMessage message) {
        return addAddressingFault(message, getInvalidAddressingHeaderFaultSubcode(),
                getInvalidAddressingHeaderFaultReason());
    }

    @Override
    public final SoapFault addMessageAddressingHeaderRequiredFault(SoapMessage message) {
        return addAddressingFault(message, getMessageAddressingHeaderRequiredFaultSubcode(),
                getMessageAddressingHeaderRequiredFaultReason());
    }

    private SoapFault addAddressingFault(SoapMessage message, QName subcode, String reason) {
        if (message.getSoapBody() instanceof Soap11Body) {
            Soap11Body soapBody = (Soap11Body) message.getSoapBody();
            return soapBody.addFault(subcode, reason, Locale.ENGLISH);
        }
        else if (message.getSoapBody() instanceof Soap12Body) {
            Soap12Body soapBody = (Soap12Body) message.getSoapBody();
            Soap12Fault soapFault =
		            soapBody.addClientOrSenderFault(reason, Locale.ENGLISH);
            soapFault.addFaultSubcode(subcode);
            return soapFault;
        }
        return null;
    }

    /*
    * Address URIs
    */

    @Override
    public final boolean hasAnonymousAddress(EndpointReference epr) {
        URI anonymous = getAnonymous();
        return anonymous != null && anonymous.equals(epr.getAddress());
    }

    @Override
    public final boolean hasNoneAddress(EndpointReference epr) {
        URI none = getNone();
        return none != null && none.equals(epr.getAddress());
    }

    /** Returns the prefix associated with the WS-Addressing namespace handled by this specification. */
    protected String getNamespacePrefix() {
        return "wsa";
    }

    /** Returns the WS-Addressing namespace handled by this specification. */
    protected abstract String getNamespaceUri();

    /*
     * Message addressing properties
     */

    /** Returns the qualified name of the {@code To} addressing header. */
    protected QName getToName() {
	    return new QName(getNamespaceUri(), "To", getNamespacePrefix());
    }

    /** Returns the qualified name of the {@code From} addressing header. */
    protected QName getFromName() {
	    return new QName(getNamespaceUri(), "From", getNamespacePrefix());
    }

    /** Returns the qualified name of the {@code ReplyTo} addressing header. */
    protected QName getReplyToName() {
	    return new QName(getNamespaceUri(), "ReplyTo", getNamespacePrefix());
    }

    /** Returns the qualified name of the {@code FaultTo} addressing header. */
    protected QName getFaultToName() {
	    return new QName(getNamespaceUri(), "FaultTo", getNamespacePrefix());
    }

    /** Returns the qualified name of the {@code Action} addressing header. */
    protected QName getActionName() {
	    return new QName(getNamespaceUri(), "Action", getNamespacePrefix());
    }

    /** Returns the qualified name of the {@code MessageID} addressing header. */
    protected QName getMessageIdName() {
	    return new QName(getNamespaceUri(), "MessageID", getNamespacePrefix());
    }

    /** Returns the qualified name of the {@code RelatesTo} addressing header. */
    protected QName getRelatesToName() {
	    return new QName(getNamespaceUri(), "RelatesTo", getNamespacePrefix());
    }

    /** Returns the qualified name of the {@code RelatesTo} addressing header. */
    protected QName getRelationshipTypeName() {
        return new QName("RelationshipType");
    }

    /**
     * Returns the qualified name of the {@code ReferenceProperties} in the endpoint reference. Returns
     * {@code null} when reference properties are not supported by this version of the spec.
     */
    protected QName getReferencePropertiesName() {
	    return new QName(getNamespaceUri(), "ReferenceProperties", getNamespacePrefix());
    }

    /**
     * Returns the qualified name of the {@code ReferenceParameters} in the endpoint reference. Returns
     * {@code null} when reference parameters are not supported by this version of the spec.
     */
    protected QName getReferenceParametersName() {
	    return new QName(getNamespaceUri(), "ReferenceParameters", getNamespacePrefix());
    }

    /*
     * Endpoint Reference
     */

    /** The qualified name of the {@code Address} in {@code EndpointReference}. */
    protected QName getAddressName() {
	    return new QName(getNamespaceUri(), "Address", getNamespacePrefix());
    }

    /** Returns the default To URI. */
    protected abstract URI getDefaultTo();

    /** Returns the default ReplyTo EPR. Can be based on the From EPR, or the anonymous URI. */
    protected abstract EndpointReference getDefaultReplyTo(EndpointReference from);

    /*
     * Address URIs
     */

    /** Returns the anonymous URI. */
    protected abstract URI getAnonymous();

    /** Returns the none URI, or {@code null} if the spec does not define it. */
    protected abstract URI getNone();

    /*
     * Faults
     */

    /** Returns the qualified name of the fault subcode that indicates that a header is missing. */
    protected abstract QName getMessageAddressingHeaderRequiredFaultSubcode();

    /** Returns the reason of the fault that indicates that a header is missing. */
    protected abstract String getMessageAddressingHeaderRequiredFaultReason();

    /** Returns the qualified name of the fault subcode that indicates that a header is invalid. */
    protected abstract QName getInvalidAddressingHeaderFaultSubcode();

    /** Returns the reason of the fault that indicates that a header is invalid. */
    protected abstract String getInvalidAddressingHeaderFaultReason();
}
