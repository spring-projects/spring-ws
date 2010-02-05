/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.server.endpoint.mapping;

import java.lang.annotation.Annotation;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.ws.server.endpoint.annotation.Endpoint;

/**
 * Abstract base for {@link org.springframework.ws.server.EndpointMapping} implementations that map classes tagged with
 * an annotation. By default the annotation is {@link Endpoint}, but this can be overriden in subclasses.
 * <p/>
 * The methods of each bean carrying @Endpoint will be registered using {@link #registerMethods(String)}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public abstract class AbstractAnnotationMethodEndpointMapping extends AbstractMethodEndpointMapping {

    private boolean detectEndpointsInAncestorContexts = false;

    /**
     * Set whether to detect endpoint beans in ancestor ApplicationContexts.
     * <p/>
     * Default is "false": Only endpoint beans in the current ApplicationContext will be detected, i.e. only in the
     * context that this EndpointMapping itself is defined in (typically the current MessageDispatcherServlet's
     * context).
     * <p/>
     * Switch this flag on to detect endpoint beans in ancestor contexts (typically the Spring root
     * WebApplicationContext) as well.
     */
    public void setDetectEndpointsInAncestorContexts(boolean detectEndpointsInAncestorContexts) {
        this.detectEndpointsInAncestorContexts = detectEndpointsInAncestorContexts;
    }

    /** Returns the 'endpoint' annotation type. Default is {@link Endpoint}. */
    protected Class<? extends Annotation> getEndpointAnnotationType() {
        return Endpoint.class;
    }

    @Override
    protected final void initApplicationContext() throws BeansException {
        if (logger.isDebugEnabled()) {
            logger.debug("Looking for endpoints in application context: " + getApplicationContext());
        }
        String[] beanNames = (this.detectEndpointsInAncestorContexts ?
                BeanFactoryUtils.beanNamesForTypeIncludingAncestors(getApplicationContext(), Object.class) :
                getApplicationContext().getBeanNamesForType(Object.class));

        for (String beanName : beanNames) {
            Class<?> endpointClass = getApplicationContext().getType(beanName);
            if (endpointClass != null &&
                    AnnotationUtils.findAnnotation(endpointClass, getEndpointAnnotationType()) != null) {
                registerMethods(beanName);
            }
        }
    }

}
