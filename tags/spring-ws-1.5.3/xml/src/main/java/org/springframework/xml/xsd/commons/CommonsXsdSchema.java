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
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaSerializer;
import org.w3c.dom.Document;

import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.Assert;
import org.springframework.xml.validation.XmlValidator;
import org.springframework.xml.validation.XmlValidatorFactory;
import org.springframework.xml.xsd.XsdSchema;

/**
 * Implementation of the {@link XsdSchema} interface that uses Apache WS-Commons XML Schema.
 *
 * @author Arjen Poutsma
 * @see <a href="http://ws.apache.org/commons/XmlSchema/">Commons XML Schema</a>
 * @since 1.5.0
 */
public class CommonsXsdSchema implements XsdSchema {

    private final XmlSchema schema;

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

    public Source getSource() {
        // try to use the the package-friendly XmlSchemaSerializer first, fall back to slower stream-based version
        try {
            XmlSchemaSerializer serializer =
                    (XmlSchemaSerializer) BeanUtils.instantiateClass(XmlSchemaSerializer.class);
            Document[] serializesSchemas = serializer.serializeSchema(schema, false);
            return new DOMSource(serializesSchemas[0]);
        }
        catch (BeanInstantiationException ex) {
            // ignore
        }
        catch (XmlSchemaSerializer.XmlSchemaSerializerException ex) {
            // ignore
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        schema.write(bos);
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        return new StreamSource(bis);
    }

    public XmlValidator createValidator() throws IOException {
        Resource resource = new UrlResource(schema.getSourceURI());
        return XmlValidatorFactory.createValidator(resource, XmlValidatorFactory.SCHEMA_W3C_XML);
    }

    /** Returns the wrapped Commons <code>XmlSchema</code> object. */
    public XmlSchema getSchema() {
        return schema;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer("CommonsXsdSchema");
        buffer.append('{');
        buffer.append(getTargetNamespace());
        buffer.append('}');
        return buffer.toString();
    }

}
