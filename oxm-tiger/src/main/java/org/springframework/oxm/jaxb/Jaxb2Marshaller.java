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

package org.springframework.oxm.jaxb;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.UUID;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;

import org.springframework.core.io.Resource;
import org.springframework.oxm.XmlMappingException;
import org.springframework.oxm.mime.MimeContainer;
import org.springframework.oxm.mime.MimeMarshaller;
import org.springframework.oxm.mime.MimeUnmarshaller;
import org.springframework.util.ClassUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.xml.transform.StaxResult;
import org.springframework.xml.transform.StaxSource;
import org.springframework.xml.validation.SchemaLoaderUtils;

/**
 * Implementation of the <code>Marshaller</code> interface for JAXB 2.0.
 * <p/>
 * The typical usage will be to set either the <code>contextPath</code> or the <code>classesToBeBound</code> property on
 * this bean, possibly customize the marshaller and unmarshaller by setting properties, schemas, adapters, and
 * listeners, and to refer to it.
 *
 * @author Arjen Poutsma
 * @see #setContextPath(String)
 * @see #setClassesToBeBound(Class[])
 * @see #setJaxbContextProperties(java.util.Map)
 * @see #setMarshallerProperties(java.util.Map)
 * @see #setUnmarshallerProperties(java.util.Map)
 * @see #setSchema(org.springframework.core.io.Resource)
 * @see #setSchemas(org.springframework.core.io.Resource[])
 * @see #setMarshallerListener(javax.xml.bind.Marshaller.Listener)
 * @see #setUnmarshallerListener(javax.xml.bind.Unmarshaller.Listener)
 * @see #setAdapters(javax.xml.bind.annotation.adapters.XmlAdapter[])
 */
public class Jaxb2Marshaller extends AbstractJaxbMarshaller implements MimeMarshaller, MimeUnmarshaller {

    private Resource[] schemaResources;

    private String schemaLanguage = XMLConstants.W3C_XML_SCHEMA_NS_URI;

    private Marshaller.Listener marshallerListener;

    private Unmarshaller.Listener unmarshallerListener;

    private XmlAdapter[] adapters;

    private Schema schema;

    private Class[] classesToBeBound;

    private Map<java.lang.String, ?> jaxbContextProperties;

    /**
     * Sets the <code>XmlAdapter</code>s to be registered with the JAXB <code>Marshaller</code> and
     * <code>Unmarshaller</code>
     */
    public void setAdapters(XmlAdapter[] adapters) {
        this.adapters = adapters;
    }

    /**
     * Sets the list of java classes to be recognized by a newly created JAXBContext. Setting this property or
     * <code>contextPath</code> is required.
     *
     * @see #setContextPath(String)
     */
    public void setClassesToBeBound(Class[] classesToBeBound) {
        this.classesToBeBound = classesToBeBound;
    }

    /**
     * Sets the <code>JAXBContext</code> properties. These implementation-specific properties will be set on the
     * <code>JAXBContext</code>.
     */
    public void setJaxbContextProperties(Map<String, ?> jaxbContextProperties) {
        this.jaxbContextProperties = jaxbContextProperties;
    }

    /** Sets the <code>Marshaller.Listener</code> to be registered with the JAXB <code>Marshaller</code>. */
    public void setMarshallerListener(Marshaller.Listener marshallerListener) {
        this.marshallerListener = marshallerListener;
    }

    /**
     * Sets the schema language. Default is the W3C XML Schema: <code>http://www.w3.org/2001/XMLSchema"</code>.
     *
     * @see XMLConstants#W3C_XML_SCHEMA_NS_URI
     * @see XMLConstants#RELAXNG_NS_URI
     */
    public void setSchemaLanguage(String schemaLanguage) {
        this.schemaLanguage = schemaLanguage;
    }

    /** Sets the schema resource to use for validation. */
    public void setSchema(Resource schemaResource) {
        schemaResources = new Resource[]{schemaResource};
    }

    /** Sets the schema resources to use for validation. */
    public void setSchemas(Resource[] schemaResources) {
        this.schemaResources = schemaResources;
    }

    /** Sets the <code>Unmarshaller.Listener</code> to be registered with the JAXB <code>Unmarshaller</code>. */
    public void setUnmarshallerListener(Unmarshaller.Listener unmarshallerListener) {
        this.unmarshallerListener = unmarshallerListener;
    }

    public boolean supports(Class clazz) {
        return clazz.getAnnotation(XmlRootElement.class) != null || JAXBElement.class.isAssignableFrom(clazz);
    }

    /*
     * JAXBContext
     */

    protected JAXBContext createJaxbContext() throws Exception {
        if (JaxbUtils.getJaxbVersion() < JaxbUtils.JAXB_2) {
            throw new IllegalStateException(
                    "Cannot use Jaxb2Marshaller in combination with JAXB 1.0. Use Jaxb1Marshaller instead.");
        }
        if (StringUtils.hasLength(getContextPath()) && !ObjectUtils.isEmpty(classesToBeBound)) {
            throw new IllegalArgumentException("specify either contextPath or classesToBeBound property; not both");
        }
        if (!ObjectUtils.isEmpty(schemaResources)) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Setting validation schema to " + StringUtils.arrayToCommaDelimitedString(schemaResources));
            }
            schema = SchemaLoaderUtils.loadSchema(schemaResources, schemaLanguage);
        }
        if (StringUtils.hasLength(getContextPath())) {
            return createJaxbContextFromContextPath();
        }
        else if (!ObjectUtils.isEmpty(classesToBeBound)) {
            return createJaxbContextFromClasses();
        }
        else {
            throw new IllegalArgumentException("setting either contextPath or classesToBeBound is required");
        }
    }

    private JAXBContext createJaxbContextFromContextPath() throws JAXBException {
        if (logger.isInfoEnabled()) {
            logger.info("Creating JAXBContext with context path [" + getContextPath() + "]");
        }
        if (jaxbContextProperties != null) {
            return JAXBContext
                    .newInstance(getContextPath(), ClassUtils.getDefaultClassLoader(), jaxbContextProperties);
        }
        else {
            return JAXBContext.newInstance(getContextPath());
        }
    }

    private JAXBContext createJaxbContextFromClasses() throws JAXBException {
        if (logger.isInfoEnabled()) {
            logger.info("Creating JAXBContext with classes to be bound [" +
                    StringUtils.arrayToCommaDelimitedString(classesToBeBound) + "]");
        }
        if (jaxbContextProperties != null) {
            return JAXBContext.newInstance(classesToBeBound, jaxbContextProperties);
        }
        else {
            return JAXBContext.newInstance(classesToBeBound);
        }
    }

    /*
     * Marshaller/Unmarshaller
     */

    protected void initJaxbMarshaller(Marshaller marshaller) throws JAXBException {
        if (schema != null) {
            marshaller.setSchema(schema);
        }
        if (marshallerListener != null) {
            marshaller.setListener(marshallerListener);
        }
        if (adapters != null) {
            for (int i = 0; i < adapters.length; i++) {
                marshaller.setAdapter(adapters[i]);
            }
        }
    }

    protected void initJaxbUnmarshaller(Unmarshaller unmarshaller) throws JAXBException {
        if (schema != null) {
            unmarshaller.setSchema(schema);
        }
        if (unmarshallerListener != null) {
            unmarshaller.setListener(unmarshallerListener);
        }
        if (adapters != null) {
            for (int i = 0; i < adapters.length; i++) {
                unmarshaller.setAdapter(adapters[i]);
            }
        }
    }

    /*
     * Marshalling
     */

    public void marshal(Object graph, Result result) throws XmlMappingException {
        marshal(graph, result, null);
    }

    public void marshal(Object graph, Result result, MimeContainer mimeContainer) throws XmlMappingException {
        try {
            Marshaller marshaller = createMarshaller();
            if (mimeContainer != null) {
                marshaller.setAttachmentMarshaller(new Jaxb2AttachmentMarshaller(mimeContainer));
            }
            if (result instanceof StaxResult) {
                marshalStaxResult(marshaller, graph, (StaxResult) result);
            }
            else {
                marshaller.marshal(graph, result);
            }
        }
        catch (JAXBException ex) {
            throw convertJaxbException(ex);
        }
    }

    private void marshalStaxResult(Marshaller jaxbMarshaller, Object graph, StaxResult staxResult)
            throws JAXBException {
        if (staxResult.getXMLStreamWriter() != null) {
            jaxbMarshaller.marshal(graph, staxResult.getXMLStreamWriter());
        }
        else if (staxResult.getXMLEventWriter() != null) {
            jaxbMarshaller.marshal(graph, staxResult.getXMLEventWriter());
        }
        else {
            throw new IllegalArgumentException("StaxResult contains neither XMLStreamWriter nor XMLEventConsumer");
        }
    }

    /*
     * Unmarshalling
     */

    public Object unmarshal(Source source) throws XmlMappingException {
        return unmarshal(source, null);
    }

    public Object unmarshal(Source source, MimeContainer mimeContainer) throws XmlMappingException {
        try {
            Unmarshaller unmarshaller = createUnmarshaller();
            if (mimeContainer != null) {
                unmarshaller.setAttachmentUnmarshaller(new Jaxb2AttachmentUnmarshaller(mimeContainer));
            }
            if (source instanceof StaxSource) {
                return unmarshalStaxSource(unmarshaller, (StaxSource) source);
            }
            else {
                return unmarshaller.unmarshal(source);
            }
        }
        catch (JAXBException ex) {
            throw convertJaxbException(ex);
        }
    }

    private Object unmarshalStaxSource(Unmarshaller jaxbUnmarshaller, StaxSource staxSource) throws JAXBException {
        if (staxSource.getXMLStreamReader() != null) {
            return jaxbUnmarshaller.unmarshal(staxSource.getXMLStreamReader());
        }
        else if (staxSource.getXMLEventReader() != null) {
            return jaxbUnmarshaller.unmarshal(staxSource.getXMLEventReader());
        }
        else {
            throw new IllegalArgumentException("StaxSource contains neither XMLStreamReader nor XMLEventReader");
        }
    }

    /*
    * Inner classes
    */

    private static class Jaxb2AttachmentMarshaller extends AttachmentMarshaller {

        private final MimeContainer mimeContainer;

        public Jaxb2AttachmentMarshaller(MimeContainer mimeContainer) {
            this.mimeContainer = mimeContainer;
        }

        public String addMtomAttachment(byte[] data,
                                        int offset,
                                        int length,
                                        String mimeType,
                                        String elementNamespace,
                                        String elementLocalName) {
            ByteArrayDataSource dataSource = new ByteArrayDataSource(mimeType, data, offset, length);
            return addMtomAttachment(new DataHandler(dataSource), elementNamespace, elementLocalName);
        }

        public String addMtomAttachment(DataHandler dataHandler, String elementNamespace, String elementLocalName) {
            String contentId = UUID.randomUUID() + "@" + elementNamespace;
            mimeContainer.addAttachment("<" + contentId + ">", dataHandler);
            return "cid:" + contentId;
        }

        public String addSwaRefAttachment(DataHandler dataHandler) {
            String contentId = UUID.randomUUID() + "@" + dataHandler.getName();
            mimeContainer.addAttachment(contentId, dataHandler);
            return contentId;
        }

        @Override
        public boolean isXOPPackage() {
            return mimeContainer.convertToXopPackage();
        }
    }

    private static class Jaxb2AttachmentUnmarshaller extends AttachmentUnmarshaller {

        private final MimeContainer mimeContainer;

        public Jaxb2AttachmentUnmarshaller(MimeContainer mimeContainer) {
            this.mimeContainer = mimeContainer;
        }

        public byte[] getAttachmentAsByteArray(String cid) {
            try {
                DataHandler dataHandler = getAttachmentAsDataHandler(cid);
                return FileCopyUtils.copyToByteArray(dataHandler.getInputStream());
            }
            catch (IOException ex) {
                throw new JaxbUnmarshallingFailureException(ex);
            }
        }

        public DataHandler getAttachmentAsDataHandler(String contentId) {
            if (contentId.startsWith("cid:")) {
                contentId = '<' + contentId.substring("cid:".length()) + '>';
            }
            return mimeContainer.getAttachment(contentId);
        }

        @Override
        public boolean isXOPPackage() {
            return mimeContainer.isXopPackage();
        }
    }

    /*
     * DataSource that wraps around a byte array
     */
    private static class ByteArrayDataSource implements DataSource {

        private byte[] data;

        private String contentType;

        private int offset;

        private int length;

        public ByteArrayDataSource(String contentType, byte[] data, int offset, int length) {
            this.contentType = contentType;
            this.data = data;
            this.offset = offset;
            this.length = length;
        }

        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(data, offset, length);
        }

        public OutputStream getOutputStream() throws IOException {
            throw new UnsupportedOperationException();
        }

        public String getContentType() {
            return contentType;
        }

        public String getName() {
            return "ByteArrayDataSource";
        }
    }

}

