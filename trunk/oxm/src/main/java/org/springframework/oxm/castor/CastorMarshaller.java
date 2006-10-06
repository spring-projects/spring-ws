/*
 * Copyright 2005 the original author or authors.
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
package org.springframework.oxm.castor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.castor.mapping.BindingType;
import org.castor.mapping.MappingUnmarshaller;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.mapping.MappingLoader;
import org.exolab.castor.xml.ClassDescriptorResolverFactory;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.UnmarshalHandler;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.XMLClassDescriptorResolver;
import org.exolab.castor.xml.XMLException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.oxm.AbstractMarshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.xml.dom.DomContentHandler;
import org.springframework.xml.stream.StaxEventContentHandler;
import org.springframework.xml.stream.StaxEventXmlReader;
import org.springframework.xml.stream.StaxStreamContentHandler;
import org.springframework.xml.stream.StaxStreamXmlReader;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

/**
 * Implementation of the <code>Marshaller</code> interface for Castor. By default, Castor does not require any further
 * configuration, though setting a target class or providing a mapping file can be used to have more control over the
 * behavior of Castor.
 * <p/>
 * If a target class is specified using <code>setTargetClass</code>, the <code>CastorMarshaller</code> can only be used
 * to unmarshall XML that represents that specific class. If you want to unmarshall multiple classes, you have to
 * provide a mapping file using <code>setMappingLocation</code>.
 * <p/>
 * Due to Castor's API, it is required to set the encoding used for writing to output streams. It defaults to
 * <code>UTF-8</code>.
 *
 * @author Arjen Poutsma
 * @see #setEncoding(String)
 * @see #setTargetClass(Class)
 * @see #setMappingLocation(org.springframework.core.io.Resource)
 */
public class CastorMarshaller extends AbstractMarshaller implements InitializingBean {

    /**
     * The default encoding used for stream access.
     */
    public static final String DEFAULT_ENCODING = "UTF-8";

    private Resource mappingLocation;

    private String encoding;

    private Class targetClass;

    private XMLClassDescriptorResolver classDescriptorResolver;

    /**
     * Returns the encoding to be used for stream access. If this property is not set, the default encoding is used.
     *
     * @see #DEFAULT_ENCODING
     */
    private String getEncoding() {
        return encoding != null ? encoding : DEFAULT_ENCODING;
    }

    /**
     * Sets the encoding to be used for stream access. If this property is not set, the default encoding is used.
     *
     * @see #DEFAULT_ENCODING
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Sets the Castor target class. If this property is set, this <code>CastorMarshaller</code> is tied to this one
     * specific class. Use a mapping file for unmarshalling multiple classes.
     * <p/>
     * You cannot set both this property and the mapping (location).
     */
    public void setTargetClass(Class targetClass) {
        this.targetClass = targetClass;
    }

    /**
     * Sets the location of the Castor XML Mapping file.
     */
    public void setMappingLocation(Resource mappingLocation) {
        this.mappingLocation = mappingLocation;
    }

    public final void afterPropertiesSet() throws IOException {
        if (mappingLocation != null && targetClass != null) {
            throw new IllegalArgumentException("Cannot set both the 'mappingLocation' and 'targetClass' property. " +
                    "Set targetClass for unmarshalling a single class, and 'mappingLocation' for multiple classes'");
        }
        if (logger.isInfoEnabled()) {
            if (mappingLocation != null) {
                logger.info("Configured using " + mappingLocation);
            }
            else if (targetClass != null) {
                logger.info("Configured for target class [" + targetClass.getName() + "]");
            }
            else {
                logger.info("Using default configuration");
            }
        }
        try {
            createClassDescriptorResolver();
        }
        catch (MappingException ex) {
            throw new CastorSystemException("Could not load Castor mapping: " + ex.getMessage(), ex);
        }
    }

    private void createClassDescriptorResolver() throws MappingException, IOException {
        classDescriptorResolver = (XMLClassDescriptorResolver) ClassDescriptorResolverFactory
                .createClassDescriptorResolver(BindingType.XML);
        if (mappingLocation != null) {
            Mapping mapping = new Mapping();
            mapping.loadMapping(new InputSource(mappingLocation.getInputStream()));
            MappingUnmarshaller mappingUnmarshaller = new MappingUnmarshaller();
            MappingLoader mappingLoader = mappingUnmarshaller.getMappingLoader(mapping, BindingType.XML);
            classDescriptorResolver.setMappingLoader(mappingLoader);
            classDescriptorResolver.setClassLoader(mapping.getClassLoader());
        }
        else if (targetClass != null) {
            classDescriptorResolver.setClassLoader(targetClass.getClassLoader());
        }
    }

    /**
     * Converts the given <code>CastorException</code> to an appropriate exception from the
     * <code>org.springframework.oxm</code> hierarchy.
     * <p/>
     * The default implementation delegates to <code>CastorUtils</code>. Can be overridden in subclasses.
     * <p/>
     * A boolean flag is used to indicate whether this exception occurs during marshalling or unmarshalling, since
     * Castor itself does not make this distinction in its exception hierarchy.
     *
     * @param ex          Castor <code>XMLException</code> that occured
     * @param marshalling indicates whether the exception occurs during marshalling (<code>true</code>), or
     *                    unmarshalling (<code>false</code>)
     * @return the corresponding <code>XmlMappingException</code>
     * @see CastorUtils#convertXmlException
     */
    public XmlMappingException convertCastorException(XMLException ex, boolean marshalling) {
        return CastorUtils.convertXmlException(ex, marshalling);
    }

    private Unmarshaller createUnmarshaller() {
        Unmarshaller unmarshaller = null;
        if (targetClass != null) {
            unmarshaller = new Unmarshaller(targetClass);
        }
        else {
            unmarshaller = new Unmarshaller();
        }
        unmarshaller.setResolver(classDescriptorResolver);
        return unmarshaller;
    }

    private void marshal(Object graph, Marshaller marshaller) {
        try {
            marshaller.setResolver(classDescriptorResolver);
            marshaller.marshal(graph);
        }
        catch (XMLException ex) {
            throw convertCastorException(ex, true);
        }
    }

    protected void marshalDomNode(Object graph, Node node) throws XmlMappingException {
        ContentHandler contentHandler = new DomContentHandler(node);
        marshalSaxHandlers(graph, contentHandler, null);
    }

    protected void marshalXmlEventWriter(Object graph, XMLEventWriter eventWriter) throws XmlMappingException {
        ContentHandler contentHandler = new StaxEventContentHandler(eventWriter);
        marshalSaxHandlers(graph, contentHandler, null);
    }

    protected void marshalXmlStreamWriter(Object graph, XMLStreamWriter streamWriter) throws XmlMappingException {
        ContentHandler contentHandler = new StaxStreamContentHandler(streamWriter);
        marshalSaxHandlers(graph, contentHandler, null);
    }

    protected void marshalOutputStream(Object graph, OutputStream outputStream)
            throws XmlMappingException, IOException {
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, getEncoding());
        marshalWriter(graph, writer);
    }

    protected void marshalSaxHandlers(Object graph, ContentHandler contentHandler, LexicalHandler lexicalHandler)
            throws XmlMappingException {
        try {
            Marshaller marshaller = new Marshaller(contentHandler);
            marshal(graph, marshaller);
        }
        catch (IOException ex) {
            throw new CastorSystemException("Could not construct Castor ContentHandler Marshaller", ex);
        }
    }

    protected void marshalWriter(Object graph, Writer writer) throws XmlMappingException, IOException {
        Marshaller marshaller = new Marshaller(writer);
        marshal(graph, marshaller);
    }

    protected Object unmarshalDomNode(Node node) throws XmlMappingException {
        try {
            return createUnmarshaller().unmarshal(node);
        }
        catch (XMLException ex) {
            throw convertCastorException(ex, false);
        }
    }

    protected Object unmarshalXmlEventReader(XMLEventReader eventReader) {
        XMLReader reader = new StaxEventXmlReader(eventReader);
        try {
            return unmarshalSaxReader(reader, new InputSource());
        }
        catch (IOException ex) {
            throw new CastorUnmarshallingFailureException(new MarshalException(ex));
        }
    }

    protected Object unmarshalXmlStreamReader(XMLStreamReader streamReader) {
        XMLReader reader = new StaxStreamXmlReader(streamReader);
        try {
            return unmarshalSaxReader(reader, new InputSource());
        }
        catch (IOException ex) {
            throw new CastorUnmarshallingFailureException(new MarshalException(ex));
        }
    }

    protected Object unmarshalSaxReader(XMLReader xmlReader, InputSource inputSource)
            throws XmlMappingException, IOException {
        UnmarshalHandler unmarshalHandler = createUnmarshaller().createHandler();
        try {
            ContentHandler contentHandler = Unmarshaller.getContentHandler(unmarshalHandler);
            xmlReader.setContentHandler(contentHandler);
            xmlReader.parse(inputSource);
            return unmarshalHandler.getObject();
        }
        catch (SAXException ex) {
            throw new CastorUnmarshallingFailureException(ex);
        }
    }

    protected Object unmarshalInputStream(InputStream inputStream) throws XmlMappingException, IOException {
        try {
            return createUnmarshaller().unmarshal(new InputSource(inputStream));
        }
        catch (XMLException ex) {
            throw convertCastorException(ex, false);
        }
    }

    protected Object unmarshalReader(Reader reader) throws XmlMappingException, IOException {
        try {
            return createUnmarshaller().unmarshal(new InputSource(reader));
        }
        catch (XMLException ex) {
            throw convertCastorException(ex, false);
        }
    }
}
