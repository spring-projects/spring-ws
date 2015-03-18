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

import java.util.Properties;
import javax.xml.transform.Source;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;
import org.springframework.ws.wsdl.wsdl11.provider.DefaultMessagesProvider;
import org.springframework.ws.wsdl.wsdl11.provider.InliningXsdSchemaTypesProvider;
import org.springframework.ws.wsdl.wsdl11.provider.SoapProvider;
import org.springframework.ws.wsdl.wsdl11.provider.SuffixBasedMessagesProvider;
import org.springframework.ws.wsdl.wsdl11.provider.SuffixBasedPortTypesProvider;
import org.springframework.xml.xsd.XsdSchema;
import org.springframework.xml.xsd.XsdSchemaCollection;

/**
 * Convenient implementation of {@link Wsdl11Definition} that creates a SOAP 1.1 or 1.2 binding based on naming
 * conventions in one or more inlined XSD schemas. Delegates to {@link InliningXsdSchemaTypesProvider}, {@link
 * DefaultMessagesProvider}, {@link SuffixBasedPortTypesProvider}, {@link SoapProvider} underneath; effectively
 * equivalent to using a {@link ProviderBasedWsdl4jDefinition} with all these providers.
 *
 * <p>Example configuration:
 * <pre>
 * &lt;bean id=&quot;airline&quot; class=&quot;org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition&quot;&gt;
 *	 &lt;property name=&quot;schema&quot;&gt;
 *	   &lt;bean class=&quot;org.springframework.xml.xsd.SimpleXsdSchema&quot;&gt;
 *		 &lt;property name=&quot;xsd&quot; value=&quot;/WEB-INF/airline.xsd&quot;/&gt;
 *	   &lt;/bean&gt;
 *	 &lt;/property&gt;
 *	 &lt;property name=&quot;portTypeName&quot; value=&quot;Airline&quot;/&gt;
 *	 &lt;property name=&quot;locationUri&quot; value=&quot;http://localhost:8080/airline/services&quot;/&gt;
 * &lt;/bean&gt;
 * </pre>
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public class DefaultWsdl11Definition implements Wsdl11Definition, InitializingBean {

	private final InliningXsdSchemaTypesProvider typesProvider = new InliningXsdSchemaTypesProvider();

	private final SuffixBasedMessagesProvider messagesProvider = new SuffixBasedMessagesProvider();

	private final SuffixBasedPortTypesProvider portTypesProvider = new SuffixBasedPortTypesProvider();

	private final SoapProvider soapProvider = new SoapProvider();

	private final ProviderBasedWsdl4jDefinition delegate = new ProviderBasedWsdl4jDefinition();

	private String serviceName;

	/** Creates a new instance of the {@link DefaultWsdl11Definition}. */
	public DefaultWsdl11Definition() {
		delegate.setTypesProvider(typesProvider);
		delegate.setMessagesProvider(messagesProvider);
		delegate.setPortTypesProvider(portTypesProvider);
		delegate.setBindingsProvider(soapProvider);
		delegate.setServicesProvider(soapProvider);
	}

	/**
	 * Sets the target namespace used for this definition.
	 *
	 * <p>Defaults to the target namespace of the defined schema.
	 */
	public void setTargetNamespace(String targetNamespace) {
		delegate.setTargetNamespace(targetNamespace);
	}

	/**
	 * Sets the single XSD schema to inline. Either this property, or {@link #setSchemaCollection(XsdSchemaCollection)
	 * schemaCollection} must be set.
	 */
	public void setSchema(XsdSchema schema) {
		typesProvider.setSchema(schema);
	}

	/**
	 * Sets the XSD schema collection to inline. Either this property, or {@link #setSchema(XsdSchema) schema} must be
	 * set.
	 */
	public void setSchemaCollection(XsdSchemaCollection schemaCollection) {
		typesProvider.setSchemaCollection(schemaCollection);
	}

	/** Sets the port type name used for this definition. Required. */
	public void setPortTypeName(String portTypeName) {
		portTypesProvider.setPortTypeName(portTypeName);
	}

	/** Sets the suffix used to detect request elements in the schema. */
	public void setRequestSuffix(String requestSuffix) {
		portTypesProvider.setRequestSuffix(requestSuffix);
		messagesProvider.setRequestSuffix(requestSuffix);
	}

	/** Sets the suffix used to detect response elements in the schema. */
	public void setResponseSuffix(String responseSuffix) {
		portTypesProvider.setResponseSuffix(responseSuffix);
		messagesProvider.setResponseSuffix(responseSuffix);
	}

	/** Sets the suffix used to detect fault elements in the schema. */
	public void setFaultSuffix(String faultSuffix) {
		portTypesProvider.setFaultSuffix(faultSuffix);
		messagesProvider.setFaultSuffix(faultSuffix);
	}

	/**
	 * Indicates whether a SOAP 1.1 binding should be created.
	 *
	 * <p>Defaults to {@code true}.
	 */
	public void setCreateSoap11Binding(boolean createSoap11Binding) {
		soapProvider.setCreateSoap11Binding(createSoap11Binding);
	}

	/**
	 * Indicates whether a SOAP 1.2 binding should be created.
	 *
	 * <p>Defaults to {@code false}.
	 */
	public void setCreateSoap12Binding(boolean createSoap12Binding) {
		soapProvider.setCreateSoap12Binding(createSoap12Binding);
	}

	/**
	 * Sets the SOAP Actions for this binding. Keys are {@link javax.wsdl.BindingOperation#getName() binding operation
	 * names}; values are {@link javax.wsdl.extensions.soap.SOAPOperation#getSoapActionURI() SOAP Action URIs}.
	 *
	 * @param soapActions the soap
	 */
	public void setSoapActions(Properties soapActions) {
		soapProvider.setSoapActions(soapActions);
	}

	/** Sets the value used for the binding transport attribute value. Defaults to HTTP. */
	public void setTransportUri(String transportUri) {
		soapProvider.setTransportUri(transportUri);
	}

	/** Sets the value used for the SOAP Address location attribute value. */
	public void setLocationUri(String locationUri) {
		soapProvider.setLocationUri(locationUri);
	}

	/**
	 * Sets the service name.
	 *
	 * <p>Defaults to the port type name, with the suffix {@code Service} appended to it.
	 */
	public void setServiceName(String serviceName) {
		soapProvider.setServiceName(serviceName);
		this.serviceName = serviceName;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (!StringUtils.hasText(delegate.getTargetNamespace()) && typesProvider.getSchemaCollection() != null &&
				typesProvider.getSchemaCollection().getXsdSchemas().length > 0) {
			XsdSchema schema = typesProvider.getSchemaCollection().getXsdSchemas()[0];
			setTargetNamespace(schema.getTargetNamespace());
		}
		if (!StringUtils.hasText(serviceName) && StringUtils.hasText(portTypesProvider.getPortTypeName())) {
			soapProvider.setServiceName(portTypesProvider.getPortTypeName() + "Service");
		}
		delegate.afterPropertiesSet();
	}

	@Override
	public Source getSource() {
		return delegate.getSource();
	}
}
