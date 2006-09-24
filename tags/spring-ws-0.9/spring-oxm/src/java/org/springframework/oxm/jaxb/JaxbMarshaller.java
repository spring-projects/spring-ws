/*
 * Copyright 2005 the original author or authors.
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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.oxm.XmlMappingException;
import org.springframework.util.Assert;

/**
 * Implementation of the <code>Marshaller</code> interface for JAXB.
 *
 * @author Arjen Poutsma
 */
public class JaxbMarshaller
        implements org.springframework.oxm.Marshaller, org.springframework.oxm.Unmarshaller, InitializingBean {

    private Marshaller marshaller;

    private Unmarshaller unmarshaller;

    private JAXBContext jaxbContext;

    /**
     * Sets the <code>JAXBContext</code>.
     */
    public void setJaxbContext(JAXBContext jaxbContext) {
        this.jaxbContext = jaxbContext;
    }

    public void marshal(Object graph, Result result) {
        try {
            marshaller.marshal(graph, result);
        }
        catch (JAXBException ex) {
            throw convertJaxbException(ex);
        }
    }

    public Object unmarshal(Source source) {
        try {
            return this.unmarshaller.unmarshal(source);
        }
        catch (JAXBException ex) {
            throw convertJaxbException(ex);
        }
    }

    public final void afterPropertiesSet() throws XmlMappingException {
        Assert.notNull(jaxbContext, "jaxbContext is required");
        try {
            marshaller = jaxbContext.createMarshaller();
            unmarshaller = jaxbContext.createUnmarshaller();
        }
        catch (JAXBException ex) {
            throw convertJaxbException(ex);
        }
    }

    /**
     * Convert the given <code>JAXBException</code> to an appropriate exception from the
     * <code>org.springframework.oxm</code> hierarchy.
     * <p/>
     * The default implementation delegates to <code>JaxbUtils</code>. Can be overridden in subclasses.
     *
     * @param ex <code>JAXBException</code> that occured
     * @return the corresponding <code>XmlMappingException</code> instance
     * @see org.springframework.oxm.jaxb.JaxbUtils#convertJaxbException
     */
    public XmlMappingException convertJaxbException(JAXBException ex) {
        return JaxbUtils.convertJaxbException(ex);
    }
}
