package org.springframework.ws.soap.saaj.support;

import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Arjen Poutsma
 */
public class SoapElementContentHandler extends DefaultHandler {

    private final SOAPEnvelope envelope;

    private SOAPElement current;

    private static final String XMLNS = "xmlns";

    public SoapElementContentHandler(SOAPElement element) {
        this.envelope = SaajUtils.getEnvelope(element);
        this.current = element;
    }

    public void characters(char ch[], int start, int length) throws SAXException {
        try {
            current.addTextNode(new String(ch, start, length));
        }
        catch (SOAPException ex) {
            throw new SAXException(ex);
        }
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        current.removeNamespaceDeclaration(prefix);
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        try {
            current.addNamespaceDeclaration(prefix, uri);
        }
        catch (SOAPException ex) {
            throw new SAXException(ex);
        }
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        try {
            Name elementName = createName(uri, qName);
            SOAPElement child = current.addChildElement(elementName);
            for (int i = 0; i < attributes.getLength(); i++) {
                Name attributeName = createName(attributes.getURI(i), attributes.getQName(i));
                if (attributeName != null) {
                    child.addAttribute(attributeName, attributes.getValue(i));
                } else {
                    int idx = attributes.getQName(i).indexOf(':');
                    String prefix = attributes.getQName(i).substring(idx+1);
                    child.addNamespaceDeclaration(prefix, attributes.getValue(i));
                }
            }
            current = child;
        }
        catch (SOAPException ex) {
            throw new SAXException(ex);
        }
    }

    private Name createName(String namespaceUri, String qualifiedName) throws SOAPException {
        int idx = qualifiedName.indexOf(':');
        String localName = qualifiedName.substring(idx + 1);
        if (idx == -1) {
            return envelope.createName(localName, "", namespaceUri);
        }
        else {
            String prefix = qualifiedName.substring(0, idx);
            if (!XMLNS.equals(prefix)) {
                return envelope.createName(localName, prefix, namespaceUri);
            }
            else {
                return null;
            }
        }

    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        current = current.getParentElement();
    }
}
