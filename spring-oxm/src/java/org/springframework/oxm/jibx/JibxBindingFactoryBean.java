/*
 * Copyright 2006 the original author or authors.
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

package org.springframework.oxm.jibx;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

/**
 * Factory bean that configures a JiBX <code>IBindingFactory</code> and provides it as bean reference.
 * <p/>
 * The typical usage will be to set the <code>targetClass</code> and optionally the <code>name</code> property on this
 * bean, and to refer to it.
 *
 * @author Arjen Poutsma
 * @see #setTargetClass(Class)
 * @see #setName(String)
 */
public class JibxBindingFactoryBean implements FactoryBean, InitializingBean {

    private static final Log logger = LogFactory.getLog(JibxBindingFactoryBean.class);

    private Class targetClass;

    private String name;

    private IBindingFactory bindingFactory;

    /**
     * Returns the singleton <code>IBindingFactory</code>.
     *
     * @return the <code>IBindingFactory</code>
     */
    public Object getObject() throws Exception {
        return bindingFactory;
    }

    /**
     * Returns the class of <code>IBindingFactory</code>.
     *
     * @return the class of <code>IBindingFactory</code>.
     */
    public Class getObjectType() {
        return IBindingFactory.class;
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
     * Sets the optional binding name for this instance.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the target class for this instance. This property is required.
     */
    public void setTargetClass(Class targetClass) {
        this.targetClass = targetClass;
    }

    public void afterPropertiesSet() throws Exception {
        if (targetClass == null) {
            throw new IllegalArgumentException("targetClass is required");
        }
        if (logger.isInfoEnabled()) {
            logger.info("Using target class [" + targetClass + "] and name [" + name + "]");
        }
        if (StringUtils.hasLength(name)) {
            bindingFactory = BindingDirectory.getFactory(name, targetClass);
        }
        else {
            bindingFactory = BindingDirectory.getFactory(targetClass);
        }
    }
}
