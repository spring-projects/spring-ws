/*
 * Copyright 2007 the original author or authors.
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

package org.springframework.xml.schema.xsd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaObjectTable;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.xml.sax.SaxUtils;
import org.springframework.xml.validation.XmlValidator;
import org.springframework.xml.validation.XmlValidatorFactory;

/**
 * Implementation of the {@link XsdSchema} interface that uses <a href="http://ws.apache.org/commons/XmlSchema/">Apache
 * WS Commons XMLSchema</a>. Allows for multiple {@link XsdSchemaDocument}s, and follows
 * <code>&lt;xsd:include/&gt;</code> and <code>&lt;xsd:import/&gt;</code> elements.
 * <p/>
 * Allows an XSD {@link Resource resource} to be set by the {@link #setSchema(Resource) schema} or {@link
 * #setSchemas(Resource[]) schemas} properties, or directly in the {@link #WsCommonsXsdSchema(Resource)} or {@link
 * #WsCommonsXsdSchema(Resource[])} constructors.
 *
 * @author Arjen Poutsma
 * @since 1.0.2
 */
public class WsCommonsXsdSchema implements XsdSchema, InitializingBean {

    private Resource[] schemaResources;

    private XsdSchemaDocument[] schemaDocuments;

    private XmlValidator validator;

    /**
     * Create a new instance of the {@link WsCommonsXsdSchema} class.
     * <p/>
     * A subsequent call to the {@link #setSchema(Resource)} or  {@link #setSchemas(Resource[])} method is required.
     */
    public WsCommonsXsdSchema() {
    }

    /**
     * Create a new instance of the {@link WsCommonsXsdSchema} class with the specified single resource.
     *
     * @param schemaResource the XSD resource; must not be <code>null</code>
     * @throws IllegalArgumentException if the supplied <code>schemaResource</code> is <code>null</code>
     */
    public WsCommonsXsdSchema(Resource schemaResource) {
        setSchema(schemaResource);
    }

    /**
     * Create a new instance of the {@link WsCommonsXsdSchema} class with the specified resources.
     *
     * @param schemaResources the XSD resources; must not be <code>null</code> or empty
     * @throws IllegalArgumentException if the supplied <code>schemaResource</code> is <code>null</code>
     */
    public WsCommonsXsdSchema(Resource[] schemaResources) {
        setSchemas(schemaResources);
    }

    /** Set the XSD resource to be used. */
    public void setSchema(Resource schemaResource) {
        this.schemaResources = new Resource[]{schemaResource};
    }

    /** Set the XSD resources to be used. */
    public void setSchemas(Resource[] schemaResources) {
        this.schemaResources = schemaResources;
    }

    public XmlValidator getValidator() {
        return validator;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notEmpty(schemaResources, "'schemaResources' must not be empty");
        XmlSchemaCollection schemaCollection = new XmlSchemaCollection();
        for (int i = 0; i < schemaResources.length; i++) {
            Resource schemaResource = schemaResources[i];
            Assert.notNull(schemaResource, "'schemaResource' must not be null");
            Assert.isTrue(schemaResource.exists(), "schema \"" + schemaResource + "\" does not exist");
            schemaCollection.read(SaxUtils.createInputSource(schemaResource), null);
        }
        XmlSchema[] schemas = schemaCollection.getXmlSchemas();
        List schemaList = new ArrayList(schemaResources.length);
        for (int i = 0; i < schemas.length; i++) {
            if (!schemas[i].getTargetNamespace().equals("http://www.w3.org/2001/XMLSchema")) {
                schemaList.add(new WsCommonsXsdSchemaDocument(schemas[i]));
            }
        }
        this.schemaDocuments = (XsdSchemaDocument[]) schemaList.toArray(new XsdSchemaDocument[schemaList.size()]);
        validator = XmlValidatorFactory.createValidator(schemaResources, XmlValidatorFactory.SCHEMA_W3C_XML);
    }

    public XsdSchemaDocument[] getSchemaDocuments() {
        return schemaDocuments;
    }

    /** Inner definition of {@link WsCommonsXsdSchemaDocument}. */
    private static class WsCommonsXsdSchemaDocument implements XsdSchemaDocument {

        private final XmlSchema commonsSchema;

        private WsCommonsXsdSchemaDocument(XmlSchema commonsSchema) {
            this.commonsSchema = commonsSchema;
        }

        public String getFilename() {
            return StringUtils.getFilename(commonsSchema.getSourceURI());
        }

        public Source getSource() {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            commonsSchema.write(bos);
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            return new StreamSource(bis);
        }

        public XsdElement[] getElements() {
            XmlSchemaObjectTable elements = commonsSchema.getElements();
            XsdElement[] declarations = new XsdElement[elements.getCount()];
            int i = 0;
            for (Iterator iterator = elements.getValues(); iterator.hasNext();) {
                XmlSchemaElement element = (XmlSchemaElement) iterator.next();
                declarations[i] = new WsCommonsXsdElement(element);
                i++;
            }
            return declarations;
        }

        public String getTargetNamespace() {
            return commonsSchema.getTargetNamespace();
        }

    }

    private static class WsCommonsXsdElement implements XsdElement {

        private final XmlSchemaElement element;

        private WsCommonsXsdElement(XmlSchemaElement element) {
            this.element = element;
        }

        public QName getName() {
            return element.getQName();
        }
    }
}
