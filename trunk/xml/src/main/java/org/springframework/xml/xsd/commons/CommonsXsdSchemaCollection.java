/*
 * Copyright 2008 the original author or authors.
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

package org.springframework.xml.xsd.commons;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.ws.commons.schema.ValidationEventHandler;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaExternal;
import org.apache.ws.commons.schema.XmlSchemaImport;
import org.apache.ws.commons.schema.XmlSchemaInclude;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.Assert;
import org.springframework.xml.sax.SaxUtils;
import org.springframework.xml.validation.XmlValidator;
import org.springframework.xml.validation.XmlValidatorFactory;
import org.springframework.xml.xsd.XsdSchema;
import org.springframework.xml.xsd.XsdSchemaCollection;

/**
 * Implementation of the {@link XsdSchemaCollection} that uses Apache WS-Commons XML Schema.
 * <p/>
 * Setting the {@link #setInline(boolean) inline} flag to <code>true</code> will result in all referenced schemas
 * (included and imported) being merged into the referred schema. When including the schemas into a WSDL, this greatly
 * simplifies the deloyment of the schemas.
 *
 * @author Arjen Poutsma
 * @see <a href="http://ws.apache.org/commons/XmlSchema/">Commons XML Schema</a>
 * @since 1.5.0
 */
public class CommonsXsdSchemaCollection implements XsdSchemaCollection, InitializingBean {

    private final XmlSchemaCollection schemaCollection = new XmlSchemaCollection();

    private final List xmlSchemas = new ArrayList();

    private Resource[] xsdResources;

    private boolean inline = false;

    private ValidationEventHandler validationEventHandler;

    /**
     * Constructs a new, empty instance of the <code>CommonsXsdSchemaCollection</code>.
     * <p/>
     * A subsequent call to the {@link #setXsds(Resource[])} is required.
     */
    public CommonsXsdSchemaCollection() {
    }

    /**
     * Constructs a new instance of the <code>CommonsXsdSchemaCollection</code> based on the given resources.
     *
     * @param resources the schema resources to load
     */
    public CommonsXsdSchemaCollection(Resource[] resources) {
        this.xsdResources = resources;
    }

    /**
     * Sets the schema resources to be loaded.
     *
     * @param xsdResources the schema resources to be loaded
     */
    public void setXsds(Resource[] xsdResources) {
        this.xsdResources = xsdResources;
    }

    /**
     * Defines whether included schemas should be inlinded into the including schema.
     * <p/>
     * Defaults to <code>false</code>.
     */
    public void setInline(boolean inline) {
        this.inline = inline;
    }

    /** Sets the WS-Commons validation event handler to use while parsing schemas. */
    public void setValidationEventHandler(ValidationEventHandler validationEventHandler) {
        this.validationEventHandler = validationEventHandler;
    }

    public void afterPropertiesSet() throws IOException {
        Assert.notEmpty(xsdResources, "'xsds' must not be empty");

        Set processedIncludes = new HashSet();
        Set processedImports = new HashSet();

        for (int i = 0; i < xsdResources.length; i++) {
            Assert.isTrue(xsdResources[i].exists(), xsdResources[i] + " does not exit");
            XmlSchema xmlSchema = schemaCollection
                    .read(SaxUtils.createInputSource(xsdResources[i]), validationEventHandler);
            xmlSchemas.add(xmlSchema);

            if (inline) {
                inlineIncludes(xmlSchema, processedIncludes, processedImports);
                findImports(xmlSchema, processedImports, processedIncludes);
            }
        }
    }

    public XsdSchema[] getXsdSchemas() {
        XsdSchema[] result = new XsdSchema[xmlSchemas.size()];
        for (int i = 0; i < xmlSchemas.size(); i++) {
            XmlSchema xmlSchema = (XmlSchema) xmlSchemas.get(i);
            result[i] = new CommonsXsdSchema(xmlSchema);
        }
        return result;
    }

    public XmlValidator createValidator() throws IOException {
        Resource[] resources = new Resource[xmlSchemas.size()];
        for (int i = xmlSchemas.size() - 1; i >= 0; i--) {
            XmlSchema xmlSchema = (XmlSchema) xmlSchemas.get(i);
            resources[i] = new UrlResource(xmlSchema.getSourceURI());
        }
        return XmlValidatorFactory.createValidator(resources, XmlValidatorFactory.SCHEMA_W3C_XML);
    }

    private void inlineIncludes(XmlSchema schema, Set processedIncludes, Set processedImports) {
        processedIncludes.add(schema);
        XmlSchemaObjectCollection includes = schema.getIncludes();
        for (int i = 0; i < includes.getCount(); i++) {
            XmlSchemaExternal external = (XmlSchemaExternal) includes
                    .getItem(i);
            if (external instanceof XmlSchemaInclude) {
                XmlSchema includedSchema = external.getSchema();
                XmlSchemaObjectCollection items = schema.getItems();
                if (!processedIncludes.contains(includedSchema)) {
                    inlineIncludes(includedSchema, processedIncludes, processedImports);
                    findImports(includedSchema, processedImports, processedIncludes);
                    XmlSchemaObjectCollection includeItems = includedSchema
                            .getItems();
                    for (int j = 0; j < includeItems.getCount(); j++) {
                        XmlSchemaObject includedItem = includeItems.getItem(j);
                        items.add(includedItem);
                    }
                }
                // remove the <include/>
                items.remove(external);
            }
        }
    }

    private void findImports(XmlSchema schema, Set processedImports, Set processedIncludes) {
        processedImports.add(schema);
        XmlSchemaObjectCollection includes = schema.getIncludes();
        for (int i = 0; i < includes.getCount(); i++) {
            XmlSchemaExternal external = (XmlSchemaExternal) includes.getItem(i);
            if (external instanceof XmlSchemaImport) {
                XmlSchemaImport schemaImport = (XmlSchemaImport) external;
                XmlSchema importedSchema = schemaImport.getSchema();
                if (!"http://www.w3.org/XML/1998/namespace".equals(schemaImport.getNamespace()) &&
                        importedSchema != null && !processedImports.contains(importedSchema)) {
                    inlineIncludes(importedSchema, processedIncludes, processedImports);
                    findImports(importedSchema, processedImports, processedIncludes);
                    xmlSchemas.add(importedSchema);
                }
                // remove the schemaLocation
                external.setSchemaLocation(null);
            }
        }
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer("CommonsXsdSchemaCollection");
        buffer.append('{');
        for (int i = 0; i < xmlSchemas.size(); i++) {
            XmlSchema schema = (XmlSchema) xmlSchemas.get(i);
            buffer.append(schema.getTargetNamespace());
            if (i < xmlSchemas.size() - 1) {
                buffer.append(',');
            }
        }
        buffer.append('}');
        return buffer.toString();
    }

}
