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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.transform.Source;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.constants.Constants;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.xml.transform.ResourceSource;
import org.springframework.xml.xsd.XsdSchema;
import org.springframework.xml.xsd.XsdSchemaCollection;

/**
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public class CommonsXsdSchemaCollection implements XsdSchemaCollection, InitializingBean {

    private static final Log logger = LogFactory.getLog(CommonsXsdSchemaCollection.class);

    private XmlSchemaCollection schemaCollection = new XmlSchemaCollection();

    private Resource[] xsdResources;

    public CommonsXsdSchemaCollection() {
    }

    public CommonsXsdSchemaCollection(Resource[] xsdResources) {
        this.xsdResources = xsdResources;
    }

    public CommonsXsdSchemaCollection(XmlSchemaCollection schemaCollection) {
        this.schemaCollection = schemaCollection;
    }

    public void setXsds(Resource[] xsdResources) {
        this.xsdResources = xsdResources;
    }

    public void afterPropertiesSet() throws Exception {
        if (!ObjectUtils.isEmpty(xsdResources)) {
            Assert.notEmpty(xsdResources, "'xsds' must not be empty");
            XMLReader xmlReader = XMLReaderFactory.createXMLReader();
            xmlReader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
            for (int i = 0; i < xsdResources.length; i++) {
                Assert.isTrue(xsdResources[i].exists(), "xsd '" + xsdResources[i] + "' does not exit");
                Source source = new ResourceSource(xmlReader, xsdResources[i]);
                schemaCollection.read(source, null);
            }
            if (logger.isInfoEnabled()) {
                logger.info("Loaded " + Arrays.asList(xsdResources));
            }
        }
    }

    public XsdSchema[] getXsdSchemas() {
        XmlSchema[] schemas = schemaCollection.getXmlSchemas();
        List result = new ArrayList(schemas.length - 1);
        for (int i = 0; i < schemas.length; i++) {
            // Ignore the main XSD schema, which is always loaded
            if (!Constants.URI_2001_SCHEMA_XSD.equals(schemas[i].getTargetNamespace())) {
                result.add(new CommonsXsdSchema(schemas[i]));
            }
        }
        return (XsdSchema[]) result.toArray(new XsdSchema[result.size()]);
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer("CommonsXsdSchemaCollection");
        buffer.append('{');
        XmlSchema[] schemas = schemaCollection.getXmlSchemas();
        for (int i = 0; i < schemas.length; i++) {
            if (!Constants.URI_2001_SCHEMA_XSD.equals(schemas[i].getTargetNamespace())) {
                buffer.append(schemas[i].getTargetNamespace());
                if (i < schemas.length - 1) {
                    buffer.append(',');
                }
            }
        }
        buffer.append('}');
        return buffer.toString();
    }


}
