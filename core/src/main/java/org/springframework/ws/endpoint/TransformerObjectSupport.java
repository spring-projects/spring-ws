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
 * Convenient base class for objects that use a <code>Transformer</code>. Subclasses can call
 * <code>createTransformer</code> to obtain a transformer. This should be done per incoming request, because
 * <code>Transformer</code> instances are not thread-safe.
 *
 * @author Arjen Poutsma
 * @see Transformer
 * @see #createTransformer()
 */
public abstract class TransformerObjectSupport {

    /**
     * Logger available to subclasses.
     */
    protected final Log logger = LogFactory.getLog(getClass());

    private static TransformerFactory transformerFactory;

    static {
        transformerFactory = TransformerFactory.newInstance();
    }

    /**
     * Creates a new <code>Transformer</code>. Must be called per request, as transformer is not thread-safe.
     *
     * @return the created transformer
     * @throws TransformerConfigurationException
     *          if thrown by JAXP methods
     */
    protected final Transformer createTransformer() throws TransformerConfigurationException {
        return transformerFactory.newTransformer();
    }

}
