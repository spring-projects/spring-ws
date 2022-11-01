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

package org.springframework.ws.wsdl.wsdl11;

import java.io.IOException;

import javax.xml.transform.Source;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.ws.wsdl.WsdlDefinitionException;
import org.springframework.xml.transform.ResourceSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * The default {@link Wsdl11Definition} implementation.
 * <p>
 * Allows a WSDL to be set by the {@link #setWsdl wsdl} property, or directly in the
 * {@link #SimpleWsdl11Definition(Resource) constructor}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public class SimpleWsdl11Definition implements Wsdl11Definition, InitializingBean {

	private Resource wsdlResource;

	/**
	 * Create a new instance of the {@link SimpleWsdl11Definition} class.
	 * <p>
	 * A subsequent call to the {@link #setWsdl(Resource)} method is required.
	 */
	public SimpleWsdl11Definition() {}

	/**
	 * Create a new instance of the {@link SimpleWsdl11Definition} class with the specified resource.
	 *
	 * @param wsdlResource the WSDL resource; must not be {@code null}
	 * @throws IllegalArgumentException if the supplied {@code wsdlResource} is {@code null}
	 */
	public SimpleWsdl11Definition(Resource wsdlResource) {
		Assert.notNull(wsdlResource, "wsdlResource must not be null");
		this.wsdlResource = wsdlResource;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.wsdlResource, "wsdl is required");
		Assert.isTrue(this.wsdlResource.exists(), "wsdl '" + this.wsdlResource + "' does not exist");
	}

	@Override
	public Source getSource() {
		try {
			XMLReader xmlReader = XMLReaderFactory.createXMLReader();
			xmlReader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
			return new ResourceSource(xmlReader, wsdlResource);
		} catch (SAXException ex) {
			throw new WsdlDefinitionException("Could not create XMLReader", ex);
		} catch (IOException ex) {
			throw new WsdlDefinitionException("Could not create source from " + this.wsdlResource, ex);
		}
	}

	/**
	 * Set the WSDL resource to be exposed by calls to this instances' {@link #getSource()} method.
	 *
	 * @param wsdlResource the WSDL resource
	 */
	public void setWsdl(Resource wsdlResource) {
		this.wsdlResource = wsdlResource;
	}

	public String toString() {
		return "SimpleWsdl11Definition " + wsdlResource;
	}

}
