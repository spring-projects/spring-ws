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

import javax.xml.bind.Element;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

/**
 * Implementation of the <code>Marshaller</code> interface for JAXB 1.0.
 * <p/>
 * The typical usage will be to set the <code>contextPath</code> property on this bean, possibly customize the
 * marshaller and unmarshaller by setting properties, and validations, and to refer to it.
 *
 * @author Arjen Poutsma
 * @see #setContextPath(String)
 * @see #setMarshallerProperties(java.util.Map)
 * @see #setUnmarshallerProperties(java.util.Map)
 * @see #setValidating(boolean)
 * @since 1.0
 */
public class Jaxb1Marshaller extends AbstractJaxbMarshaller implements InitializingBean {

    private boolean validating = false;

    /** Set if the JAXB <code>Unmarshaller</code> should validate the incoming document. Default is <code>false</code>. */
    public void setValidating(boolean validating) {
        this.validating = validating;
    }

    public boolean supports(Class clazz) {
        return Element.class.isAssignableFrom(clazz);
    }

    protected final JAXBContext createJaxbContext() throws JAXBException {
        if (!StringUtils.hasLength(getContextPath())) {
            throw new IllegalArgumentException("contextPath is required");
        }
        if (logger.isInfoEnabled()) {
            logger.info("Creating JAXBContext with context path [" + getContextPath() + "]");
        }
        return JAXBContext.newInstance(getContextPath());
    }

    protected void initJaxbUnmarshaller(Unmarshaller unmarshaller) throws JAXBException {
        unmarshaller.setValidating(validating);
    }

    public void marshal(Object graph, Result result) {
        try {
            createMarshaller().marshal(graph, result);
        }
        catch (JAXBException ex) {
            throw convertJaxbException(ex);
        }
    }

    public Object unmarshal(Source source) {
        try {
            return createUnmarshaller().unmarshal(source);
        }
        catch (JAXBException ex) {
            throw convertJaxbException(ex);
        }
    }
}
