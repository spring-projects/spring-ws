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

package org.springframework.ws.wsdl.wsdl11;

import java.util.Iterator;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaObjectTable;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaExternal;
import org.apache.ws.commons.schema.XmlSchemaInclude;
import org.apache.ws.commons.schema.XmlSchemaImport;
import org.w3c.dom.Document;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.xml.sax.SaxUtils;

/**
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public class CommonXsdTest extends TestCase {

    public void testIt() throws Exception {
        XmlSchemaCollection collection = new XmlSchemaCollection();
        Resource r = new ClassPathResource("A.xsd", getClass());
        XmlSchema schema = collection.read(SaxUtils.createInputSource(r), null);
        handleSchema(schema);
        schema.write(System.out);
        r = new ClassPathResource("D.xsd", getClass());
        schema = collection.read(SaxUtils.createInputSource(r), null);
        handleSchema(schema);
        schema.write(System.out);
    }

    private void handleSchema(XmlSchema schema) {
        if ("http://www.w3.org/2001/XMLSchema".equals(schema.getTargetNamespace())) {
            return;
        }
        XmlSchemaObjectCollection includes = schema.getIncludes();
        for (int i = 0; i < includes.getCount(); i++) {
            XmlSchemaExternal external = (XmlSchemaExternal) includes.getItem(i);
            if (external instanceof XmlSchemaInclude) {
                XmlSchema includedSchema = external.getSchema();
                handleSchema(includedSchema);
                XmlSchemaObjectCollection includesItems = includedSchema.getItems();
                for (int j = 0; j < includesItems.getCount(); j++) {
                    schema.getItems().add(includesItems.getItem(j));
                }
//                includes.remove(external);
                schema.getItems().remove(external);
            }
        }
    }

    private void dumpSchemaObject(XmlSchemaObject obj) {
        System.out.println(obj);
        if (obj instanceof XmlSchemaSimpleType) {
            XmlSchemaSimpleType simpleType = (XmlSchemaSimpleType) obj;
            XmlSchemaSimpleTypeRestriction restriction = (XmlSchemaSimpleTypeRestriction) simpleType.getContent();
            System.out.println("simple type with base name " + restriction.getBaseTypeName());
        } else if (obj instanceof XmlSchemaComplexType) {
            XmlSchemaComplexType complexType = (XmlSchemaComplexType) obj;
            XmlSchemaSequence seq = (XmlSchemaSequence) complexType.getParticle();
            System.out.println("complex type containing sequence");
            XmlSchemaObjectCollection seqCol = seq.getItems();
            for (int j = 0; j < seqCol.getCount(); j++) {
                XmlSchemaElement element = (XmlSchemaElement) seqCol.getItem(j);
                dumpSchemaObject(element.getSchemaType());
            }
        }
    }
}