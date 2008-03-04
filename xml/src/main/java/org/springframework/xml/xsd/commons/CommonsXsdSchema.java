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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.xml.sax.SAXException;

import org.springframework.util.Assert;
import org.springframework.xml.xsd.XsdSchema;

/**
 * Implementation of the {@link XsdSchema} interface that uses Apache WS-Commons XML Schema.
 *
 * @author Arjen Poutsma
 * @see <a href="http://ws.apache.org/commons/XmlSchema/">Commons XML Schema</a>
 * @since 1.5.0
 */
public class CommonsXsdSchema implements XsdSchema {

    private XmlSchema schema;

    /**
     * Create a new instance of the  {@link CommonsXsdSchema} class with the specified {@link XmlSchema} reference.
     *
     * @param schema the Commons <code>XmlSchema</code> object; must not be <code>null</code>
     * @throws IllegalArgumentException if the supplied <code>schema</code> is <code>null</code>
     */
    protected CommonsXsdSchema(XmlSchema schema) {
        Assert.notNull(schema, "'schema' must not be null");
        this.schema = schema;
    }

    public String getTargetNamespace() {
        return schema.getTargetNamespace();
    }

    public QName[] getElementNames() {
        List result = new ArrayList();
        Iterator iterator = schema.getElements().getNames();
        while (iterator.hasNext()) {
            QName name = (QName) iterator.next();
            result.add(name);
        }
        return (QName[]) result.toArray(new QName[result.size()]);
    }

    public void merge(XsdSchema o) {
        Assert.isInstanceOf(CommonsXsdSchema.class, o);
        XmlSchema otherSchema = ((CommonsXsdSchema) o).schema;
        Assert.isTrue(this.schema.getTargetNamespace().equals(otherSchema.getTargetNamespace()),
                "Schema does not have same namespace");
        XmlSchemaObjectCollection otherItems = otherSchema.getItems();
        for (int i = 0; i < otherItems.getCount(); i++) {
            schema.getItems().add(otherItems.getItem(i));
        }
    }

    public Source getSource() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        schema.write(bos);
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        return new StreamSource(bis);
    }

    /** Returns the wrapped Commons <code>XmlSchema</code> object. */
    public XmlSchema getSchema() {
        return schema;
    }

    private void loadSchema() throws SAXException, IOException {
        XmlSchemaCollection schemaCollection = new XmlSchemaCollection();
//        this.schema = schemaCollection.read(SaxUtils.createInputSource(xsdResource), null);
    }

    /*
    public XsdSchema[] inline() {
        XmlSchema clone = cloneSchema(schema);
        inlineIncludes(clone, new ArrayList());
        return new XsdSchema[]{new CommonsXsdSchema(clone)};
    }

    private static XmlSchema cloneSchema(XmlSchema schema)  {
        XmlSchemaCollection schemaCollection = new XmlSchemaCollection();
        XmlSchema clone = new XmlSchema(schemaCollection);
        XmlSchemaObjectCollection originalItems = schema.getItems();
        XmlSchemaObjectCollection cloneItems = clone.getItems();
        for (int i = 0; i < originalItems.getCount(); i++) {
            cloneItems.add(originalItems.getItem(i));
        }
        return clone;
    }

    private static void inlineIncludes(XmlSchema schema, List processedSchemas) {
        processedSchemas.add(schema);
        XmlSchemaObjectCollection includes = schema.getIncludes();
        for (int i = 0; i < includes.getCount(); i++) {
            if (includes.getItem(i) instanceof XmlSchemaInclude) {
                XmlSchemaInclude include = (XmlSchemaInclude) includes.getItem(i);
                XmlSchema includedSchema = include.getSchema();
                XmlSchemaObjectCollection items = schema.getItems();
                if (!processedSchemas.contains(includedSchema)) {
                    inlineIncludes(includedSchema, processedSchemas);
                    XmlSchemaObjectCollection includesItems = includedSchema.getItems();
                    for (int j = 0; j < includesItems.getCount(); j++) {
                        XmlSchemaObject includedItem = includesItems.getItem(j);
                        items.add(includedItem);
                    }
                }
                // remove the <include/>
                items.remove(include);
            }
        }
    }*/

    public String toString() {
        StringBuffer buffer = new StringBuffer("CommonsXsdSchema");
        buffer.append('{');
        buffer.append(getTargetNamespace());
        buffer.append('}');
        return buffer.toString();
    }

}
