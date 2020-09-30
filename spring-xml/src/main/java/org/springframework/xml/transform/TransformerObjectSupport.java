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

package org.springframework.xml.transform;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Convenient base class for objects that use a {@code Transformer}. Subclasses can call {@link #createTransformer()} or
 * {@link #transform(Source, Result)}. This should be done per thread (i.e. per incoming request), because
 * {@code Transformer} instances are not thread-safe.
 *
 * @author Arjen Poutsma
 * @see Transformer
 * @since 1.0.0
 */
public abstract class TransformerObjectSupport {

	/**
	 * Logger available to subclasses.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	private TransformerHelper transformerHelper = new TransformerHelper();

	/**
	 * Specify the {@code TransformerFactory} class to use.
	 */
	public void setTransformerFactoryClass(Class<? extends TransformerFactory> transformerFactoryClass) {
		transformerHelper.setTransformerFactoryClass(transformerFactoryClass);
	}

	/**
	 * Instantiate a new TransformerFactory.
	 * <p>
	 * The default implementation simply calls {@link TransformerFactory#newInstance()}. If a
	 * {@link #setTransformerFactoryClass "transformerFactoryClass"} has been specified explicitly, the default
	 * constructor of the specified class will be called instead.
	 * <p>
	 * Can be overridden in subclasses.
	 *
	 * @param transformerFactoryClass the specified factory class (if any)
	 * @return the new TransactionFactory instance
	 * @see #setTransformerFactoryClass
	 * @see #getTransformerFactory()
	 */
	protected TransformerFactory newTransformerFactory(Class<? extends TransformerFactory> transformerFactoryClass) {
		return transformerHelper.newTransformerFactory(transformerFactoryClass);
	}

	/**
	 * Returns the {@code TransformerFactory}.
	 */
	protected TransformerFactory getTransformerFactory() {
		return transformerHelper.getTransformerFactory();
	}

	/**
	 * Creates a new {@code Transformer}. Must be called per request, as transformers are not thread-safe.
	 *
	 * @return the created transformer
	 * @throws TransformerConfigurationException if thrown by JAXP methods
	 */
	protected final Transformer createTransformer() throws TransformerConfigurationException {
		return transformerHelper.createTransformer();
	}

	/**
	 * Transforms the given {@link Source} to the given {@link Result}. Creates a new {@link Transformer} for every call,
	 * as transformers are not thread-safe.
	 *
	 * @param source the source to transform from
	 * @param result the result to transform to
	 * @throws TransformerException if thrown by JAXP methods
	 */
	protected final void transform(Source source, Result result) throws TransformerException {
		transformerHelper.transform(source, result);
	}

}
