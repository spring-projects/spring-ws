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

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.UnmarshalHandler;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.XMLException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.oxm.AbstractMarshaller;
import org.springframework.oxm.XmlMappingException;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

/**
 * Implementation of the <code>Marshaller</code> interface for Castor.
 * <p/>
 * Due to Castor's API, it is required to set the encoding used for writing. It defaults to <code>UTF-8</code>.
 *
 * @author Arjen Poutsma
 * @see #setEncoding(String)
 */
public class CastorMarshaller extends AbstractMarshaller implements InitializingBean {

    /**
     * The default encoding used for stream access.
     */
    public static final String DEFAULT_ENCODING = "UTF-8";

    private Resource mappingLocation;

    private String encoding;

    private Mapping mapping;

    private Unmarshaller unmarshaller;

    /**
     * Returns the encoding to be used for stream access. If this property is not set, the default encoding is used.
     *
     * @see #DEFAULT_ENCODING
     */
    private String getEncoding() {
        return (encoding != null) ? encoding : DEFAULT_ENCODING;
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
     * Sets the Castor mapping. If this property is set, the <code>mappingLocation</code> will be ignored.
     *
     * @see #setMappingLocation
     */
    public void setMapping(Mapping mapping) {
        this.mapping = mapping;
    }

    /**
     * Sets the location of the Castor XML Mapping file.
     */
    public void setMappingLocation(Resource mappingLocation) {
        this.mappingLocation = mappingLocation;
    }

    public final void afterPropertiesSet() throws Exception {
        if ((mappingLocation != null) && (mapping == null)) {
            Mapping castorMapping = new Mapping();
            InputStream inputStream = mappingLocation.getInputStream();
            try {
                castorMapping.loadMapping(new InputSource(inputStream));
                this.mapping = castorMapping;
            }
            catch (MappingException ex) {
                throw new CastorSystemException("Could not load Castor mapping: " + ex.getMessage(), ex);
            }
            finally {
                inputStream.close();
            }
        }
        if (mapping != null) {
            try {
                unmarshaller = new Unmarshaller(mapping);
            }
            catch (MappingException ex) {
                throw new CastorSystemException("Could not load Castor mapping: " + ex.getMessage(), ex);
            }
        }
        else {
            unmarshaller = new Unmarshaller();
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

    private void marshal(Object graph, Marshaller marshaller) {
        try {
            if (mapping != null) {
                marshaller.setMapping(mapping);
            }
            marshaller.marshal(graph);
        }
        catch (MappingException ex) {
            throw new CastorSystemException("Could not load Castor mapping", ex);
        }
        catch (XMLException ex) {
            throw convertCastorException(ex, true);
        }
    }

    protected void marshalDomNode(Object graph, Node node) throws XmlMappingException {
        Marshaller marshaller = new Marshaller(node);
        marshal(graph, marshaller);
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
            return unmarshaller.unmarshal(node);
        }
        catch (XMLException ex) {
            throw convertCastorException(ex, false);
        }
    }

    protected Object unmarshalSaxReader(XMLReader xmlReader, InputSource inputSource)
            throws XmlMappingException, IOException {
        UnmarshalHandler unmarshalHandler = unmarshaller.createHandler();
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
            return unmarshaller.unmarshal(new InputSource(inputStream));
        }
        catch (XMLException ex) {
            throw convertCastorException(ex, false);
        }
    }

    protected Object unmarshalReader(Reader reader) throws XmlMappingException, IOException {
        try {
            return unmarshaller.unmarshal(new InputSource(reader));
        }
        catch (XMLException ex) {
            throw convertCastorException(ex, false);
        }
    }
}
