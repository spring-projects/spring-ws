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
 *  * limitations under the License.
 */

package org.springframework.ws.soap.saaj;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.Name;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;

import org.springframework.ws.soap.saaj.support.SaajUtils;
import org.springframework.ws.soap.saaj.support.SoapElementContentHandler;
import org.springframework.ws.soap.saaj.support.SoapElementXmlReader;
import org.springframework.xml.namespace.QNameUtils;
import org.xml.sax.InputSource;

/**
 * SAAJ 1.1 implementation of the <code>SaajImplementationStrategy</code>.
 *
 * @author Arjen Poutsma
 */
public class Saaj11ImplementationStrategy implements SaajImplementationStrategy {

    public QName getName(SOAPElement element) {
        return SaajUtils.toQName(element.getElementName());
    }

    public Source getSource(SOAPElement element) {
        return new SAXSource(new SoapElementXmlReader(element), new InputSource());
    }

    public Result getResult(SOAPElement element) {
        return new SAXResult(new SoapElementContentHandler(element));
    }

    public QName getFaultCode(SOAPFault fault) {
        String code = fault.getFaultCode();
        int idx = code.indexOf(':');
        if (idx == -1) {
            return new QName(code);
        }
        else {
            String prefix = code.substring(0, idx);
            String localPart = code.substring(idx + 1);
            String namespace = fault.getNamespaceURI(prefix);
            return QNameUtils.createQName(namespace, localPart, prefix);
        }
    }

    public DetailEntry addDetailEntry(Detail detail, QName name) throws SOAPException {
        return detail.addDetailEntry(SaajUtils.toName(name, detail));
    }

    public void removeContents(SOAPElement element) {
        List children = new ArrayList();
        for (Iterator iterator = element.getChildElements(); iterator.hasNext();) {
            Node node = (Node) iterator.next();
            children.add(node);
        }
        for (Iterator iterator = children.iterator(); iterator.hasNext();) {
            Node node = (Node) iterator.next();
            node.detachNode();
        }
    }

    public SOAPHeaderElement addHeaderElement(SOAPHeader header, QName name) throws SOAPException {
        Name saajName = SaajUtils.toName(name, header);
        return header.addHeaderElement(saajName);
    }

    public Locale getFaultStringLocale(SOAPFault saajFault) {
        return null;
    }

    public SOAPFault addFault(SOAPBody saajBody, QName faultCode, String faultString, Locale faultStringLocale) throws SOAPException {
        SOAPFault fault = saajBody.addFault();
        String faultCodeQName = QNameUtils.toQualifiedName(faultCode);
        fault.setFaultCode(faultCodeQName);
        fault.setFaultString(faultString);
        return fault;
    }

    public Iterator examineMustUnderstandHeaderElements(SOAPHeader header, String role) {
        List result = new ArrayList();
        for (Iterator iterator = header.examineHeaderElements(role); iterator.hasNext();) {
            SOAPHeaderElement headerElement = (SOAPHeaderElement) iterator.next();
            if (headerElement.getMustUnderstand()) {
                result.add(headerElement);
            }
        }
        return result.iterator();
    }
}
