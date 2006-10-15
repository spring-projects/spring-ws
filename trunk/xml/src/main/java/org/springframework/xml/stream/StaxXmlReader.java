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
 * limitations under the License.
 */

package org.springframework.xml.stream;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * Abstract base class for SAX <code>XMLReader</code> implementations that use StAX as a basis.
 *
 * @author Arjen Poutsma
 * @see #setContentHandler(org.xml.sax.ContentHandler)
 * @see #setDTDHandler(org.xml.sax.DTDHandler)
 * @see #setEntityResolver(org.xml.sax.EntityResolver)
 * @see #setErrorHandler(org.xml.sax.ErrorHandler)
 */
public abstract class StaxXmlReader implements XMLReader {

    private DTDHandler dtdHandler;

    private ContentHandler contentHandler;

    private EntityResolver entityResolver;

    private ErrorHandler errorHandler;

    public ContentHandler getContentHandler() {
        return contentHandler;
    }

    public void setContentHandler(ContentHandler contentHandler) {
        this.contentHandler = contentHandler;
    }

    public void setDTDHandler(DTDHandler dtdHandler) {
        this.dtdHandler = dtdHandler;
    }

    public DTDHandler getDTDHandler() {
        return dtdHandler;
    }

    public EntityResolver getEntityResolver() {
        return entityResolver;
    }

    public void setEntityResolver(EntityResolver entityResolver) {
        this.entityResolver = entityResolver;
    }

    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    /**
     * Throws a <code>SAXNotRecognizedException</code> exception.
     *
     * @throws SAXNotRecognizedException always
     */
    public boolean getFeature(String name) throws SAXNotRecognizedException {
        throw new SAXNotRecognizedException(name);
    }

    /**
     * Throws a <code>SAXNotRecognizedException</code> exception.
     *
     * @throws SAXNotRecognizedException always
     */
    public void setFeature(String name, boolean value) throws SAXNotRecognizedException {
        throw new SAXNotRecognizedException(name);
    }

    /**
     * Throws a <code>SAXNotRecognizedException</code> exception.
     *
     * @throws SAXNotRecognizedException always
     */
    public Object getProperty(String name) throws SAXNotRecognizedException {
        throw new SAXNotRecognizedException(name);
    }

    /**
     * Throws a <code>SAXNotRecognizedException</code> exception.
     *
     * @throws SAXNotRecognizedException always
     */
    public void setProperty(String name, Object value) throws SAXNotRecognizedException {
        throw new SAXNotRecognizedException(name);
    }

    /**
     * Parses the StAX XML reader passed at construction-time.
     * <p/>
     * <strong>Note</strong> that the given <code>InputSource</code> is not read, but ignored.
     *
     * @param ignored is ignored
     * @throws SAXException A SAX exception, possibly wrapping a <code>XMLStreamException</code>
     */
    public final void parse(InputSource ignored) throws SAXException {
        parse();
    }

    /**
     * Parses the StAX XML reader passed at construction-time.
     * <p/>
     * <strong>Note</strong> that the given system identifier is not read, but ignored.
     *
     * @param ignored is ignored
     * @throws SAXException A SAX exception, possibly wrapping a <code>XMLStreamException</code>
     */
    public final void parse(String ignored) throws SAXException {
        parse();
    }

    private void parse() throws SAXException {
        try {
            parseInternal();
        }
        catch (XMLStreamException ex) {
            SAXParseException saxException = new SAXParseException(ex.getMessage(), null, null,
                    ex.getLocation().getLineNumber(), ex.getLocation().getColumnNumber(), ex);
            if (errorHandler != null) {
                errorHandler.fatalError(saxException);
            }
            else {
                throw saxException;
            }
        }
    }

    /**
     * Sets the SAX <code>Locator</code> based on the given StAX <code>Location</code>.
     *
     * @param location the location
     * @see ContentHandler#setDocumentLocator(org.xml.sax.Locator)
     */
    protected void setLocator(Location location) {
        if (contentHandler != null) {
            contentHandler.setDocumentLocator(new StaxLocator(location));
        }
    }

    /**
     * Template-method that parses the StAX reader passed at construction-time.
     */
    protected abstract void parseInternal() throws SAXException, XMLStreamException;

    /**
     * Implementation of the <code>Locator</code> interface that is based on a StAX <code>Location</code>.
     *
     * @see Locator
     * @see Location
     */
    private static class StaxLocator implements Locator {

        private Location location;

        protected StaxLocator(Location location) {
            this.location = location;
        }

        public String getPublicId() {
            return location.getPublicId();
        }

        public String getSystemId() {
            return location.getSystemId();
        }

        public int getLineNumber() {
            return location.getLineNumber();
        }

        public int getColumnNumber() {
            return location.getColumnNumber();
        }
    }
}
