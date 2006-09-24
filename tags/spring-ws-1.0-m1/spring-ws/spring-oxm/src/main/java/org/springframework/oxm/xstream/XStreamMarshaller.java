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

package org.springframework.oxm.xstream;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.CompactWriter;
import com.thoughtworks.xstream.io.xml.DomReader;
import com.thoughtworks.xstream.io.xml.DomWriter;
import com.thoughtworks.xstream.io.xml.SaxWriter;
import com.thoughtworks.xstream.io.xml.XppReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

import org.springframework.beans.propertyeditors.ClassEditor;
import org.springframework.oxm.AbstractMarshaller;
import org.springframework.oxm.XmlMappingException;

/**
 * Implementation of the <code>Marshaller</code> interface for XStream. By default, XStream does not require any further
 * configuration, though class aliases can be used to have more control over the behavior of XStream.
 * <p/>
 * Due to XStream's API, it is required to set the encoding used for writing to outputstreams. It defaults to
 * <code>UTF-8</code>.
 * <p/>
 * <b>Note</b> that XStream is an XML serialization library, not a data binding library. Therefore, it has limited
 * namespace support. As such, it is rather unsuitable for usage within Web services.
 *
 * @author Peter Meijer
 * @author Arjen Poutsma
 * @see #setEncoding(String)
 * @see #DEFAULT_ENCODING
 * @see #setAliases(java.util.Map)
 */
public class XStreamMarshaller extends AbstractMarshaller {

    /**
     * The default encoding used for stream access.
     */
    public static final String DEFAULT_ENCODING = "UTF-8";

    private XStream xstream = new XStream();

    private String encoding;

    /**
     * Returns the encoding to be used for stream access. If this property is not set, the default encoding is used.
     *
     * @see #DEFAULT_ENCODING
     */
    public String getEncoding() {
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
     * Set a alias/type map, consisting of string aliases mapped to <code>Class</code> instances (or Strings to be
     * converted to <code>Class</code> instances).
     *
     * @see ClassEditor
     */
    public void setAliases(Map aliases) {
        for (Iterator iterator = aliases.keySet().iterator(); iterator.hasNext();) {
            String name = (String) iterator.next();
            Object value = aliases.get(name);
            // Check whether we need to convert from String to Class.
            Class type = null;
            if (value instanceof Class) {
                type = (Class) value;
            }
            else {
                ClassEditor editor = new ClassEditor();
                editor.setAsText(String.valueOf(value));
                type = (Class) editor.getValue();
            }
            addAlias(name, type);
        }
    }

    /**
     * Adds an alias for the given type.
     *
     * @param name alias to be used for the type
     * @param type the type to be aliased
     */
    public void addAlias(String name, Class type) {
        xstream.alias(name, type);
    }

    /**
     * Convert the given XStream exception to an appropriate exception from the <code>org.springframework.oxm</code>
     * hierarchy. <p/> The default implementation delegates to <code>XStreamUtils</code>. Can be overridden in
     * subclasses.
     *
     * @param ex          exception that occured
     * @param marshalling indicates whether the exception occurs during marshalling (<code>true</code>), or
     *                    unmarshalling (<code>false</code>)
     * @return the corresponding <code>XmlMappingException</code> instance
     * @see XStreamUtils#convertXStreamException(Exception, boolean)
     */
    public XmlMappingException convertXStreamException(Exception ex, boolean marshalling) {
        return XStreamUtils.convertXStreamException(ex, marshalling);
    }

    /**
     * Marshals the given graph to the given XStream HierarchicalStreamWriter. Converts exceptions using
     * <code>convertXStreamException</code>.
     */
    private void marshal(Object graph, HierarchicalStreamWriter streamWriter) {
        try {
            xstream.marshal(graph, streamWriter);
        }
        catch (Exception ex) {
            throw convertXStreamException(ex, true);
        }
    }

    protected void marshalDomNode(Object graph, Node node) throws XmlMappingException {
        HierarchicalStreamWriter streamWriter = null;
        if (node instanceof Document) {
            streamWriter = new DomWriter((Document) node);
        }
        else if (node instanceof Element) {
            streamWriter = new DomWriter((Element) node);
        }
        else {
            throw new IllegalArgumentException("DOMResult contains neither Document nor Element");
        }
        marshal(graph, streamWriter);
    }

    protected void marshalOutputStream(Object graph, OutputStream outputStream)
            throws XmlMappingException, IOException {
        marshalWriter(graph, new OutputStreamWriter(outputStream, getEncoding()));
    }

    protected void marshalSaxHandlers(Object graph, ContentHandler contentHandler, LexicalHandler lexicalHandler)
            throws XmlMappingException {
        SaxWriter saxWriter = new SaxWriter();
        saxWriter.setContentHandler(contentHandler);
        marshal(graph, saxWriter);
    }

    protected void marshalWriter(Object graph, Writer writer) throws XmlMappingException, IOException {
        marshal(graph, new CompactWriter(writer));
    }

    private Object unmarshal(HierarchicalStreamReader streamReader) {
        try {
            return xstream.unmarshal(streamReader);
        }
        catch (Exception ex) {
            throw convertXStreamException(ex, false);
        }
    }

    protected Object unmarshalDomNode(Node node) throws XmlMappingException {
        HierarchicalStreamReader streamReader = null;
        if (node instanceof Document) {
            streamReader = new DomReader((Document) node);
        }
        else if (node instanceof Element) {
            streamReader = new DomReader((Element) node);
        }
        else {
            throw new IllegalArgumentException("DOMSource contains neither Document nor Element");
        }
        return unmarshal(streamReader);
    }

    protected Object unmarshalInputStream(InputStream inputStream) throws XmlMappingException, IOException {
        return unmarshalReader(new InputStreamReader(inputStream, getEncoding()));
    }

    protected Object unmarshalReader(Reader reader) throws XmlMappingException, IOException {
        return unmarshal(new XppReader(reader));
    }

    protected Object unmarshalSaxReader(XMLReader xmlReader, InputSource inputSource)
            throws XmlMappingException, IOException {
        throw new UnsupportedOperationException(
                "XStreamMarshaller does not support unmarshalling using SAX XMLReaders");
    }
}
