/*
 * Copyright 2005-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.wsdl.wsdl11.provider;

import java.io.IOException;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.ws.wsdl.WsdlDefinitionException;
import org.springframework.xml.transform.TransformerObjectSupport;

/**
 * Implementation of the {@link BindingsProvider} interface that attaches a policy
 * document, such as a WS-Policy {@code Policy} or {@code PolicyReference} element, to
 * every {@link Binding} in the WSDL.
 * <p>
 * This provider delegates the actual creation of the bindings to another
 * {@link BindingsProvider}, and adds the given {@link #setPolicy(Resource) policy} to
 * each of the bindings that provider creates. The policy is added as-is, without any
 * validation of its content; it is up to the user to supply a well-formed policy
 * document, such as a WS-Policy or WS-SecurityPolicy fragment.
 *
 * @author Stephane Nicoll
 * @since 5.1.0
 * @see <a href="https://www.w3.org/TR/ws-policy/">Web Services Policy 1.5 - Framework</a>
 */
public class PolicyProvider extends TransformerObjectSupport implements BindingsProvider {

	private @Nullable BindingsProvider delegate;

	private @Nullable Resource policy;

	/**
	 * Set the {@link BindingsProvider} to delegate the creation of the bindings to.
	 * @param delegate the delegate to call
	 */
	public void setDelegate(BindingsProvider delegate) {
		this.delegate = delegate;
	}

	/**
	 * Set the policy document to attach to every binding created by the
	 * {@link #setDelegate(BindingsProvider) delegate}. The resource is expected to
	 * contain a single, well-formed XML element, such as a WS-Policy {@code Policy} or
	 * {@code PolicyReference} element.
	 */
	public void setPolicy(Resource policy) {
		this.policy = policy;
	}

	@Override
	public void addBindings(Definition definition) throws WSDLException {
		Assert.notNull(this.delegate, "'delegate' must not be null");
		this.delegate.addBindings(definition);
		if (this.policy != null) {
			attachPolicy(definition, readPolicyElement(this.policy));
		}
	}

	private void attachPolicy(Definition definition, Element policyElement) throws WSDLException {
		QName elementType = new QName(policyElement.getNamespaceURI(), policyElement.getLocalName());
		ExtensionRegistry extensionRegistry = definition.getExtensionRegistry();
		extensionRegistry.mapExtensionTypes(Binding.class, elementType, UnknownExtensibilityElement.class);
		for (Object value : definition.getBindings().values()) {
			Binding binding = (Binding) value;
			UnknownExtensibilityElement policyExtension = (UnknownExtensibilityElement) extensionRegistry
				.createExtension(Binding.class, elementType);
			policyExtension.setElementType(elementType);
			policyExtension.setElement(policyElement);
			binding.addExtensibilityElement(policyExtension);
		}
	}

	private Element readPolicyElement(Resource policy) {
		try {
			DOMResult result = new DOMResult();
			transform(new StreamSource(policy.getInputStream()), result);
			Document document = (Document) result.getNode();
			return document.getDocumentElement();
		}
		catch (TransformerException | IOException ex) {
			throw new WsdlDefinitionException("Could not read policy from " + policy, ex);
		}
	}

}
