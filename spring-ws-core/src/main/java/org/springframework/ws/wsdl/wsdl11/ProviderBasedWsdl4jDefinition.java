/*
 * Copyright 2005-2014 the original author or authors.
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

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.ws.wsdl.wsdl11.provider.BindingsProvider;
import org.springframework.ws.wsdl.wsdl11.provider.ImportsProvider;
import org.springframework.ws.wsdl.wsdl11.provider.MessagesProvider;
import org.springframework.ws.wsdl.wsdl11.provider.PortTypesProvider;
import org.springframework.ws.wsdl.wsdl11.provider.ServicesProvider;
import org.springframework.ws.wsdl.wsdl11.provider.TypesProvider;

/**
 * Implementation of the {@link Wsdl11Definition} that uses a provider-based mechanism to populate a WSDL4J
 * {@link Definition}.
 * <p>
 * All providers are optional, indicating that a particular part of the WSDL will not be created. Providers can be set
 * via various properties. The providers are {@link #afterPropertiesSet() invoked} in the following order:
 * <ol>
 * <li>{@link ImportsProvider}</li>
 * <li>{@link TypesProvider}</li>
 * <li>{@link MessagesProvider}</li>
 * <li>{@link PortTypesProvider}</li>
 * <li>{@link BindingsProvider}</li>
 * <li>{@link ServicesProvider}</li>
 * </ol>
 * <p>
 * This definition requires the target namespace to be set via {@link #setTargetNamespace(String)}
 *
 * @author Arjen Poutsma
 * @see #setImportsProvider(ImportsProvider)
 * @see #setTypesProvider(TypesProvider)
 * @see #setMessagesProvider(MessagesProvider)
 * @see #setPortTypesProvider(PortTypesProvider)
 * @see #setBindingsProvider(BindingsProvider)
 * @see #setServicesProvider(ServicesProvider)
 * @since 1.5.0
 */
public class ProviderBasedWsdl4jDefinition extends Wsdl4jDefinition implements InitializingBean {

	/** The prefix used to register the target namespace in the WSDL. */
	public static final String TARGET_NAMESPACE_PREFIX = "tns";

	private ImportsProvider importsProvider;

	private TypesProvider typesProvider;

	private MessagesProvider messagesProvider;

	private PortTypesProvider portTypesProvider;

	private BindingsProvider bindingsProvider;

	private ServicesProvider servicesProvider;

	private String targetNamespace;

	/**
	 * Returns the {@link ImportsProvider} for this definition.
	 * <p>
	 * Default is {@code null}, indicating that no {@code &lt;import&gt;} will be created
	 *
	 * @return the import provider; or {@code null}
	 */
	public ImportsProvider getImportsProvider() {
		return importsProvider;
	}

	/**
	 * Sets the {@link ImportsProvider} for this definition.
	 * <p>
	 * Default is {@code null}, indicating that no {@code &lt;import&gt;} will be created
	 *
	 * @param importsProvider the import provider
	 */
	public void setImportsProvider(ImportsProvider importsProvider) {
		this.importsProvider = importsProvider;
	}

	/**
	 * Returns the {@link TypesProvider} for this definition.
	 * <p>
	 * Defaults to {@code null}, indicating that no {@code &lt;types&gt;} will be created
	 *
	 * @return the types provider; or {@code null}
	 */
	public TypesProvider getTypesProvider() {
		return typesProvider;
	}

	/**
	 * Sets the {@link TypesProvider} for this definition.
	 * <p>
	 * Defaults to {@code null}, indicating that no {@code &lt;types&gt;} will be created
	 *
	 * @param typesProvider the types provider; or {@code null}
	 */
	public void setTypesProvider(TypesProvider typesProvider) {
		this.typesProvider = typesProvider;
	}

	/**
	 * Returns the {@link MessagesProvider} for this definition.
	 * <p>
	 * Defaults to {@code null}, indicating that no {@code &lt;message&gt;} will be created
	 *
	 * @return the messages provider; or {@code null}
	 */
	public MessagesProvider getMessagesProvider() {
		return messagesProvider;
	}

	/**
	 * Sets the {@link MessagesProvider} for this definition.
	 * <p>
	 * Defaults to {@code null}, indicating that no {@code &lt;message&gt;} will be created
	 *
	 * @param messagesProvider the messages provider; or {@code null}
	 */
	public void setMessagesProvider(MessagesProvider messagesProvider) {
		this.messagesProvider = messagesProvider;
	}

	/**
	 * Returns the {@link PortTypesProvider} for this definition.
	 * <p>
	 * Defaults to {@code null}, indicating that no {@code &lt;portType&gt;} will be created
	 *
	 * @return the port types provider; or {@code null}
	 */
	public PortTypesProvider getPortTypesProvider() {
		return portTypesProvider;
	}

	/**
	 * Sets the {@link PortTypesProvider} for this definition.
	 * <p>
	 * Defaults to {@code null}, indicating that no {@code &lt;portType&gt;} will be created
	 *
	 * @param portTypesProvider the port types provider; or {@code null}
	 */
	public void setPortTypesProvider(PortTypesProvider portTypesProvider) {
		this.portTypesProvider = portTypesProvider;
	}

	/**
	 * Returns the {@link BindingsProvider} for this definition.
	 * <p>
	 * Defaults to {@code null}, indicating that no {@code &lt;binding&gt;} will be created
	 *
	 * @return the binding provider; or {@code null}
	 */
	public BindingsProvider getBindingsProvider() {
		return bindingsProvider;
	}

	/**
	 * Sets the {@link BindingsProvider} for this definition.
	 * <p>
	 * Defaults to {@code null}, indicating that no {@code &lt;binding&gt;} will be created
	 *
	 * @param bindingsProvider the bindings provider; or {@code null}
	 */
	public void setBindingsProvider(BindingsProvider bindingsProvider) {
		this.bindingsProvider = bindingsProvider;
	}

	/**
	 * Returns the {@link ServicesProvider} for this definition.
	 * <p>
	 * Defaults to {@code null}, indicating that no {@code &lt;service&gt;} will be created
	 *
	 * @return the services provider; or {@code null}
	 */
	public ServicesProvider getServicesProvider() {
		return servicesProvider;
	}

	/**
	 * Sets the {@link ServicesProvider} for this definition.
	 * <p>
	 * Defaults to {@code null}, indicating that no {@code &lt;service&gt;} will be created
	 *
	 * @param servicesProvider the services provider; or {@code null}
	 */
	public void setServicesProvider(ServicesProvider servicesProvider) {
		this.servicesProvider = servicesProvider;
	}

	/**
	 * Returns the target namespace for the WSDL definition.
	 *
	 * @return the target namespace
	 * @see javax.wsdl.Definition#getTargetNamespace()
	 */
	public String getTargetNamespace() {
		return targetNamespace;
	}

	/**
	 * Sets the target namespace used for this definition. Required.
	 *
	 * @param targetNamespace the target namespace
	 * @see javax.wsdl.Definition#setTargetNamespace(String)
	 */
	public void setTargetNamespace(String targetNamespace) {
		this.targetNamespace = targetNamespace;
	}

	@Override
	public void afterPropertiesSet() throws WSDLException {
		Assert.notNull(getTargetNamespace(), "'targetNamespace' is required");
		WSDLFactory wsdlFactory = WSDLFactory.newInstance();
		Definition definition = wsdlFactory.newDefinition();
		definition.setTargetNamespace(getTargetNamespace());
		definition.addNamespace(TARGET_NAMESPACE_PREFIX, getTargetNamespace());
		if (importsProvider != null) {
			importsProvider.addImports(definition);
		}
		if (typesProvider != null) {
			typesProvider.addTypes(definition);
		}
		if (messagesProvider != null) {
			messagesProvider.addMessages(definition);
		}
		if (portTypesProvider != null) {
			portTypesProvider.addPortTypes(definition);
		}
		if (bindingsProvider != null) {
			bindingsProvider.addBindings(definition);
		}
		if (servicesProvider != null) {
			servicesProvider.addServices(definition);
		}
		setDefinition(definition);
	}
}
