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

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

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
public class Jaxb2Marshaller extends AbstractJaxbMarshaller {

    private Resource[] schemaResources;

    private String schemaLanguage = XMLConstants.W3C_XML_SCHEMA_NS_URI;

    private Marshaller.Listener marshallerListener;

    private Unmarshaller.Listener unmarshallerListener;

    private XmlAdapter[] adapters;

    private Schema schema;

    private Class[] classesToBeBound;

    private Map jaxbContextProperties;

    /**
     * Sets the <code>JAXBContext</code> properties. These implementation-specific properties will be set on the
     * <code>JAXBContext</code>.
     */
    public void setJaxbContextProperties(Map jaxbContextProperties) {
        this.jaxbContextProperties = jaxbContextProperties;
    }

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
     * Sets the <code>Marshaller.Listener</code> to be registered with the JAXB <code>Marshaller</code>.
     */
    public void setMarshallerListener(Marshaller.Listener marshallerListener) {
        this.marshallerListener = marshallerListener;
    }

    /**
     * Sets the schema resource to use for validation.
     */
    public void setSchema(Resource schemaResource) {
        this.schemaResources = new Resource[]{schemaResource};
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

    /**
     * Sets the schema resources to use for validation.
     */
    public void setSchemas(Resource[] schemaResources) {
        this.schemaResources = schemaResources;
    }

    /**
     * Sets the <code>Unmarshaller.Listener</code> to be registered with the JAXB <code>Unmarshaller</code>.
     */
    public void setUnmarshallerListener(Unmarshaller.Listener unmarshallerListener) {
        this.unmarshallerListener = unmarshallerListener;
    }

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

    protected JAXBContext createJaxbContext() throws Exception {
        if (JaxbUtils.getJaxbVersion() < JaxbUtils.JAXB_2) {
            throw new IllegalStateException(
                    "Cannot use Jaxb2Marshaller in combination with JAXB 1.0. Use Jaxb1Marshaller instead.");
        }
        JAXBContext jaxbContext;
        if (StringUtils.hasLength(getContextPath())) {
            if (logger.isInfoEnabled()) {
                logger.info("Creating JAXBContext with context path [" + getContextPath() + "]");
            }
            if (jaxbContextProperties != null) {
                jaxbContext = JAXBContext
                        .newInstance(getContextPath(), ClassUtils.getDefaultClassLoader(), jaxbContextProperties);
            }
            else {
                jaxbContext = JAXBContext.newInstance(getContextPath());
            }
        }
        else if (!ObjectUtils.isEmpty(classesToBeBound)) {
            if (logger.isInfoEnabled()) {
                logger.info("Creating JAXBContext with classes to be bound [" +
                        StringUtils.arrayToCommaDelimitedString(classesToBeBound) + "]");
            }
            if (jaxbContextProperties != null) {
                jaxbContext = JAXBContext.newInstance(classesToBeBound, jaxbContextProperties);
            }
            else {
                jaxbContext = JAXBContext.newInstance(classesToBeBound);
            }
        }
        else {
            throw new IllegalArgumentException("setting either contextPath or classesToBeBound is required");
        }
        createSchema();

        return jaxbContext;
    }

    private void createSchema() throws IOException, SAXException {
        if (!ObjectUtils.isEmpty(schemaResources)) {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(schemaLanguage);
            StreamSource[] schemaSources = new StreamSource[schemaResources.length];
            try {
                for (int i = 0; i < schemaResources.length; i++) {
                    Resource schemaResource = schemaResources[i];
                    if (logger.isDebugEnabled()) {
                        logger.debug("Setting validation schema to " + schemaResource);
                    }
                    schemaSources[i] = new StreamSource(schemaResource.getInputStream());
                }
                schema = schemaFactory.newSchema(schemaSources);
            }
            finally {
                for (int i = 0; i < schemaSources.length; i++) {
                    if (schemaSources[i] != null) {
                        InputStream inputStream = schemaSources[i].getInputStream();
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    }
                }
            }
        }
    }
}
