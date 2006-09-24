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

package org.springframework.ws.endpoint;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract base class for all endpoints that use a <code>Transformer</code>. Subclasses can call
 * <code>createTransformer</code> to obtain a transformer. This should be done per incoming request, because
 * <code>Transformer</code> instances are not thread-safe
 *
 * @author Arjen Poutsma
 * @see Transformer
 * @see #createTransformer()
 */
public abstract class AbstractTransformingEndpoint {

    /**
     * Logger available to subclasses.
     */
    protected final Log logger = LogFactory.getLog(getClass());

    private TransformerFactory transformerFactory;

    /**
     * Creates a new <code>Transformer</code>. Must be called per request, as transformer is not thread-safe.
     *
     * @return the created transformer
     * @throws TransformerConfigurationException
     *          if thrown by JAXP methods
     */
    protected final Transformer createTransformer() throws TransformerConfigurationException {
        if (transformerFactory == null) {
            transformerFactory = createTransformerFactory();
        }
        return transformerFactory.newTransformer();
    }

    /**
     * Create a <code>TransformerFactory</code> that this endpoint will use to create <code>Transformer</code>s. Can be
     * overridden in subclasses, adding further initialization of the factory. The resulting
     * <code>TransformerFactory</code> is cached, so this method will only be called once.
     *
     * @return the created <code>TransformerFactory</code>
     * @throws TransformerFactoryConfigurationError
     *          if thrown by JAXP methods
     */
    protected TransformerFactory createTransformerFactory() throws TransformerFactoryConfigurationError {
        return TransformerFactory.newInstance();
    }

}
