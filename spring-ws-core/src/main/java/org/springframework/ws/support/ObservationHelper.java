/*
 * Copyright 2005-2024 the original author or authors.
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
package org.springframework.ws.support;

import io.micrometer.common.util.internal.logging.WarnThenDebugLogger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ws.server.endpoint.observation.ObservationInterceptor;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;

/**
 * Helper class for observation tasks.
 * @author Johan Kindgren
 */
public class ObservationHelper {

    private final Log logger = LogFactory.getLog(getClass());

    private static final WarnThenDebugLogger WARN_THEN_DEBUG_LOGGER = new WarnThenDebugLogger(ObservationInterceptor.class);
    private static final QName UNKNOWN_Q_NAME = new QName("unknown", "unknow");

    private final SAXParser saxParser;

    public ObservationHelper() {
        SAXParserFactory parserFactory = SAXParserFactory.newNSInstance();
        SAXParser parser = null;
        try {
            parser = parserFactory.newSAXParser();
        } catch (ParserConfigurationException | SAXException e) {
            logger.warn("Could not create SAX parser, observation keys for Root element can be reported as 'unknown'.", e);
        }
        saxParser = parser;
    }


    /**
     * Try to find the root element QName for the given source.
     * If it isn't possible to extract the QName, a QName with the values 'unknown:unknown' is returned.
     */
    public QName getRootElement(Source source) {
        if (source instanceof DOMSource) {
            Node document = ((DOMSource) source).getNode();
            if (document.getNodeType() == Node.DOCUMENT_NODE) {
                Document doc = (Document) document;
                Node root = doc.getDocumentElement();
                if (root != null) {
                    return new QName(root.getNamespaceURI(), root.getLocalName());
                }
            }
            return UNKNOWN_Q_NAME;
        }
        if (source instanceof StreamSource) {
            if (saxParser == null) {
                WARN_THEN_DEBUG_LOGGER.log("SaxParser not available, reporting Root element as 'unknown'");
                return UNKNOWN_Q_NAME;
            }
            RootElementSAXHandler handler = new RootElementSAXHandler();
            try {
                saxParser.parse(getInputSource((StreamSource) source), handler);
                return handler.getRootElementName();
            } catch (SAXException | IOException e) {
                WARN_THEN_DEBUG_LOGGER.log("Exception while handling request, reporting Root element as 'unknown'", e);
                return UNKNOWN_Q_NAME;
            }
        }
        if (source instanceof SAXSource) {
            if (saxParser == null) {
                WARN_THEN_DEBUG_LOGGER.log("SaxParser not available, reporting Root element as 'unknown'");
                return UNKNOWN_Q_NAME;
            }
            RootElementSAXHandler handler = new RootElementSAXHandler();
            try {
                saxParser.parse(getInputSource((SAXSource) source), handler);
                return handler.getRootElementName();
            } catch (SAXException | IOException e) {
                WARN_THEN_DEBUG_LOGGER.log("Exception while handling request, reporting Root element as 'unknown'", e);
                return UNKNOWN_Q_NAME;
            }
        }
        return UNKNOWN_Q_NAME;
    }

    InputSource getInputSource(StreamSource source) {

        if (source.getInputStream() != null) {
            return new InputSource(source.getInputStream());
        }
        return new InputSource(source.getReader());
    }

    InputSource getInputSource(SAXSource source) {
        return source.getInputSource();
    }



    /**
     * DefaultHandler that extracts the root elements namespace and name.
     * @author Johan Kindgren
     */
    static class RootElementSAXHandler extends DefaultHandler {

        private QName rootElementName = null;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            if (rootElementName == null) {
                rootElementName = new QName(uri, localName);
            }
        }

        public QName getRootElementName() {
            return rootElementName;
        }
    }
}
