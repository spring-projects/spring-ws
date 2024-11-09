package org.springframework.ws.client.core.observation;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.namespace.QName;

public class RootElementSAXHandler extends DefaultHandler {

    private QName rootElementName = null;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if (rootElementName == null) {
            // Spara rotnodens localName och namespaceURI
            rootElementName = new QName(uri, localName);
        }
    }

    public QName getRootElementName() {
        return rootElementName;
    }
}
