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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.constants.Constants;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.xml.sax.SaxUtils;
import org.springframework.xml.xsd.XsdSchema;
import org.springframework.xml.xsd.XsdSchemaCollection;

/**
 * Implementation of the {@link XsdSchemaCollection} that uses Apache WS-Commons XML Schema.
 *
 * @author Arjen Poutsma
 * @see <a href="http://ws.apache.org/commons/XmlSchema/">Commons XML Schema</a>
 * @since 1.5.0
 */
public class CommonsXsdSchemaCollection implements XsdSchemaCollection, InitializingBean {

    private static final Log logger = LogFactory.getLog(CommonsXsdSchemaCollection.class);

    private final XmlSchemaCollection schemaCollection = new XmlSchemaCollection();

    private final List xmlSchemas = new ArrayList();

    private Resource[] xsdResources;

    private boolean inlineIncludes = false;

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
    public void setInlineIncludes(boolean inlineIncludes) {
        this.inlineIncludes = inlineIncludes;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notEmpty(xsdResources, "'xsds' must not be empty");
        for (int i = 0; i < xsdResources.length; i++) {
            Assert.isTrue(xsdResources[i].exists(), xsdResources[i] + " does not exit");
            xmlSchemas.add(schemaCollection.read(SaxUtils.createInputSource(xsdResources[i]), null));
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
