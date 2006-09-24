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

package org.springframework.oxm.jibx;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.oxm.AbstractMarshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.util.Assert;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;

/**
 * Implementation of the <code>Marshaller</code> and <code>Unmarshaller</code> interfaces for JiBX. The
 *
 * @author Arjen Poutsma
 */
public class JibxMarshaller extends AbstractMarshaller implements InitializingBean {

    private IMarshallingContext marshallingContext;

    private IUnmarshallingContext unmarshallingContext;

    private IBindingFactory bindingFactory;

    /**
     * Sets the JiBX binding factory, required for usage.
     *
     * @see JibxBindingFactoryBean
     */
    public void setBindingFactory(IBindingFactory bindingFactory) {
        this.bindingFactory = bindingFactory;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(bindingFactory, "bindingFactory is required");
        try {
            marshallingContext = bindingFactory.createMarshallingContext();
            unmarshallingContext = bindingFactory.createUnmarshallingContext();
            bindingFactory = null;
        }
        catch (JiBXException ex) {
            throw new JibxSystemException(ex);
        }
    }

    /**
     * Convert the given <code>JiBXException</code> to an appropriate exception from the
     * <code>org.springframework.oxm</code> hierarchy.
     * <p/>
     * The default implementation delegates to <code>JibxUtils</code>. Can be overridden in subclasses.
     * <p/>
     * A boolean flag is used to indicate whether this exception occurs during marshalling or unmarshalling, since JiBX
     * itself does not make this distinction in its exception hierarchy.
     *
     * @param ex          <code>JiBXException</code> that occured
     * @param marshalling indicates whether the exception occurs during marshalling (<code>true</code>), or
     *                    unmarshalling (<code>false</code>)
     * @return the corresponding <code>XmlMappingException</code> instance
     * @see JibxUtils#convertJibxException(org.jibx.runtime.JiBXException, boolean)
     */
    public XmlMappingException convertJibxException(JiBXException ex, boolean marshalling) {
        return JibxUtils.convertJibxException(ex, marshalling);
    }

    protected void marshalDomNode(Object graph, Node node, Node nextSibling) throws XmlMappingException {
        throw new UnsupportedOperationException("JibxMarshaller does not support marshalling of DOM Nodes");
    }

    protected void marshalOutputStream(Object graph, OutputStream outputStream)
            throws XmlMappingException, IOException {
        try {
            marshallingContext.marshalDocument(graph, null, null, outputStream);
        }
        catch (JiBXException ex) {
            throw convertJibxException(ex, true);
        }
    }

    protected void marshalSaxHandlers(Object graph, ContentHandler contentHandler, LexicalHandler lexicalHandler)
            throws XmlMappingException {
        throw new UnsupportedOperationException("JibxMarshaller does not support marshalling of SAX Handlers");
    }

    protected void marshalWriter(Object graph, Writer writer) throws XmlMappingException, IOException {
        try {
            marshallingContext.marshalDocument(graph, null, null, writer);
        }
        catch (JiBXException ex) {
            throw convertJibxException(ex, true);
        }
    }

    protected Object unmarshalDomNode(Node node) throws XmlMappingException {
        throw new UnsupportedOperationException("JibxMarshaller does not support unmarshalling of DOM Nodes");
    }

    protected Object unmarshalInputStream(InputStream inputStream) throws XmlMappingException, IOException {
        try {
            return unmarshallingContext.unmarshalDocument(inputStream, null);
        }
        catch (JiBXException ex) {
            throw convertJibxException(ex, false);
        }
    }

    protected Object unmarshalReader(Reader reader) throws XmlMappingException, IOException {
        try {
            return unmarshallingContext.unmarshalDocument(reader);
        }
        catch (JiBXException ex) {
            throw convertJibxException(ex, false);
        }
    }
}
