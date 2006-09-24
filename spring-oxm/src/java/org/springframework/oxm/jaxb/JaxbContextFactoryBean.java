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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

/**
 * Factory bean that configures a <code>JAXBContext</code> and provides it as bean reference.
 * <p/>
 * The typical usage will be to set the <code>contextPath</code> property on this bean, and to refer to it.
 *
 * @author Arjen Poutsma
 * @see #setContextPath
 */
public class JaxbContextFactoryBean implements FactoryBean, InitializingBean {

    private static final Log logger = LogFactory.getLog(JaxbContextFactoryBean.class);

    private String contextPath;

    private JAXBContext jaxbContext;

    /**
     * Returns the singleton <code>JAXBContext</code>.
     *
     * @return the <code>JAXBContext</code>
     */
    public Object getObject() throws Exception {
        return this.jaxbContext;
    }

    /**
     * Returns the class of <code>JAXBContext</code>.
     *
     * @return the class of <code>JAXBContext</code>.
     */
    public Class getObjectType() {
        return JAXBContext.class;
    }

    /**
     * Returns <code>true</code>.
     *
     * @return <code>true</code>
     */
    public boolean isSingleton() {
        return true;
    }

    /**
     * Sets the JAXB Context path.
     */
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    /**
     * Create the JAXBContext and store it.
     */
    public void afterPropertiesSet() throws JAXBException {
        if (!StringUtils.hasLength(contextPath)) {
            throw new IllegalArgumentException("contextPath is required");
        }
        if (logger.isInfoEnabled()) {
            logger.info("Using context path [" + contextPath + "]");
        }
        this.jaxbContext = JAXBContext.newInstance(contextPath);
    }
}
