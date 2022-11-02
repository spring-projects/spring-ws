/*
 * Copyright 2005-2022 the original author or authors.
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

import org.springframework.util.Assert;

/**
 * Helper class for {@link Transformer} usage. Provides {@link #createTransformer()} and
 * {@link #transform(Source, Result)}.
 *
 * @author Arjen Poutsma
 * @since 3.0
 */
public class TransformerHelper {

	private volatile TransformerFactory transformerFactory;

	private Class<? extends TransformerFactory> transformerFactoryClass;

	/**
	 * Initializes a new instance of the {@code TransformerHelper}.
	 */
	public TransformerHelper() {}

	/**
	 * Initializes a new instance of the {@code TransformerHelper} with the specified {@link TransformerFactory}.
	 */
	public TransformerHelper(TransformerFactory transformerFactory) {
		this.transformerFactory = transformerFactory;
	}

	/**
	 * Initializes a new instance of the {@code TransformerHelper} with the specified {@link TransformerFactory} class.
	 */
	public TransformerHelper(Class<? extends TransformerFactory> transformerFactoryClass) {
		setTransformerFactoryClass(transformerFactoryClass);
	}

	/**
	 * Specify the {@code TransformerFactory} class to use.
	 */
	public void setTransformerFactoryClass(Class<? extends TransformerFactory> transformerFactoryClass) {
		Assert.isAssignable(TransformerFactory.class, transformerFactoryClass);
		this.transformerFactoryClass = transformerFactoryClass;
	}

	/**
	 * Instantiate a new TransformerFactory.
	 * <p>
	 * The default implementation simply calls {@link TransformerFactory#newInstance()}. If a
	 * {@link #setTransformerFactoryClass transformerFactoryClass} has been specified explicitly, the default constructor
	 * of the specified class will be called instead.
	 * <p>
	 * Can be overridden in subclasses.
	 *
	 * @param transformerFactoryClass the specified factory class (if any)
	 * @return the new TransactionFactory instance
	 * @see #setTransformerFactoryClass
	 * @see #getTransformerFactory()
	 */
	protected TransformerFactory newTransformerFactory(Class<? extends TransformerFactory> transformerFactoryClass) {
		if (transformerFactoryClass != null) {
			TransformerFactory transformerFactory = TransformerFactoryUtils.newInstance(transformerFactoryClass);
			return transformerFactory;
		} else {
			TransformerFactory transformerFactory = TransformerFactoryUtils.newInstance();
			return transformerFactory;
		}
	}

	/**
	 * Returns the {@code TransformerFactory}.
	 *
	 * @return the transformer factory
	 */
	public TransformerFactory getTransformerFactory() {
		TransformerFactory result = transformerFactory;
		if (result == null) {
			synchronized (this) {
				result = transformerFactory;
				if (result == null) {
					transformerFactory = result = newTransformerFactory(transformerFactoryClass);
				}
			}
		}
		return result;
	}

	/**
	 * Creates a new {@code Transformer}. Must be called per thread, as transformers are not thread-safe.
	 *
	 * @return the created transformer
	 * @throws TransformerConfigurationException if thrown by JAXP methods
	 */
	public Transformer createTransformer() throws TransformerConfigurationException {
		return getTransformerFactory().newTransformer();
	}

	/**
	 * Transforms the given {@link Source} to the given {@link Result}. Creates a new {@link Transformer} for every call,
	 * as transformers are not thread-safe.
	 *
	 * @param source the source to transform from
	 * @param result the result to transform to
	 * @throws TransformerException if thrown by JAXP methods
	 */
	public void transform(Source source, Result result) throws TransformerException {
		Transformer transformer = createTransformer();
		transformer.transform(source, result);
	}

}
