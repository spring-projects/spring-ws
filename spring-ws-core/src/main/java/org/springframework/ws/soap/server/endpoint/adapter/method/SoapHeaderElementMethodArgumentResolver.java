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

package org.springframework.ws.soap.server.endpoint.adapter.method;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.jspecify.annotations.Nullable;

import org.springframework.core.MethodParameter;
import org.springframework.oxm.GenericUnmarshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.util.Assert;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.adapter.method.MethodArgumentResolver;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.server.endpoint.annotation.SoapHeader;
import org.springframework.xml.namespace.QNameUtils;

/**
 * Implementation of {@link MethodArgumentResolver} that supports resolving
 * {@link SoapHeaderElement} parameters. Target method parameters must be annotated with
 * {@link SoapHeader} to indicate the SOAP header to resolve. This resolver supports
 * simple {@link SoapHeaderElement} parameters and {@link List} parameters for elements
 * that appear multiple times in the same SOAP header.
 * <p>
 * If an {@link Unmarshaller} has been configured, this resolver also supports parameters
 * of any type supported by that unmarshaller (and {@link List} parameters thereof), in
 * which case the matching header's content is unmarshalled into the target type.
 * <p>
 * The following snippet shows an example of supported declarations. <pre>
 * {@code
 * public void soapHeaderElement(@SoapHeader("{http://springframework.org/ws}header") SoapHeaderElement element)
 *
 * public void soapHeaderElementList(@SoapHeader("{http://springframework.org/ws}header") List<SoapHeaderElement> elements)
 *
 * public void soapHeaderUnmarshalled(@SoapHeader("{http://springframework.org/ws}header") MyHeader header)
 * } </pre>
 *
 * @author Tareq Abedrabbo
 * @author Arjen Poutsma
 * @since 2.0
 * @see SoapHeader
 */
public class SoapHeaderElementMethodArgumentResolver implements MethodArgumentResolver {

	private @Nullable Unmarshaller unmarshaller;

	/**
	 * Set the unmarshaller used for unmarshalling SOAP header content into method
	 * parameters that are not of type {@link SoapHeaderElement}.
	 * @since 5.1.0
	 */
	public void setUnmarshaller(@Nullable Unmarshaller unmarshaller) {
		this.unmarshaller = unmarshaller;
	}

	/**
	 * Return the unmarshaller used for unmarshalling SOAP header content into method
	 * parameters that are not of type {@link SoapHeaderElement}.
	 * @since 5.1.0
	 */
	public @Nullable Unmarshaller getUnmarshaller() {
		return this.unmarshaller;
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		SoapHeader soapHeader = parameter.getParameterAnnotation(SoapHeader.class);
		if (soapHeader == null) {
			return false;
		}

		Class<?> parameterType = parameter.getParameterType();

		// Simple SoapHeaderElement parameter
		if (SoapHeaderElement.class.equals(parameterType)) {
			return true;
		}

		// List<SoapHeaderElement> or List<T> parameter, with T supported by the
		// unmarshaller
		if (List.class.equals(parameterType)) {
			Type genericType = parameter.getGenericParameterType();
			if (genericType instanceof ParameterizedType parameterizedType) {
				Type[] typeArguments = parameterizedType.getActualTypeArguments();
				if (typeArguments.length != 1) {
					return false;
				}
				if (SoapHeaderElement.class.equals(typeArguments[0])) {
					return true;
				}
				return typeArguments[0] instanceof Class<?> elementType && supportsUnmarshalling(elementType);
			}
			return false;
		}

		// Any type supported by the unmarshaller
		return supportsUnmarshalling(parameterType);
	}

	private boolean supportsUnmarshalling(Class<?> type) {
		if (this.unmarshaller == null) {
			return false;
		}
		if (this.unmarshaller instanceof GenericUnmarshaller genericUnmarshaller) {
			return genericUnmarshaller.supports(type);
		}
		return this.unmarshaller.supports(type);
	}

	@Override
	public @Nullable Object resolveArgument(MessageContext messageContext, MethodParameter parameter) throws Exception {
		Assert.isInstanceOf(SoapMessage.class, messageContext.getRequest());
		SoapMessage request = (SoapMessage) messageContext.getRequest();
		org.springframework.ws.soap.SoapHeader soapHeader = request.getSoapHeader();

		SoapHeader parameterAnnotation = parameter.getParameterAnnotation(SoapHeader.class);
		Assert.state(parameterAnnotation != null, "Cannot resolve parameter, @SoapHeader annotation is required");
		String paramValue = parameterAnnotation.value();

		Assert.isTrue(QNameUtils.validateQName(paramValue), "Invalid header qualified name [" + paramValue + "]. "
				+ "QName must be of the form '{namespace}localPart'.");
		QName qname = QName.valueOf(paramValue);
		Class<?> parameterType = parameter.getParameterType();
		if (soapHeader == null) {
			throw new IllegalStateException("SOAP header is null");
		}

		if (SoapHeaderElement.class.equals(parameterType)) {
			return extractSoapHeader(qname, soapHeader);
		}
		else if (List.class.equals(parameterType)) {
			Type genericType = parameter.getGenericParameterType();
			Type elementType = ((ParameterizedType) genericType).getActualTypeArguments()[0];
			if (SoapHeaderElement.class.equals(elementType)) {
				return extractSoapHeaderList(qname, soapHeader);
			}
			Unmarshaller unmarshaller = this.unmarshaller;
			Assert.state(unmarshaller != null, "'unmarshaller' must be set to extract SOAP header with custom type");
			List<Object> result = new ArrayList<>();
			for (SoapHeaderElement element : extractSoapHeaderList(qname, soapHeader)) {
				result.add(unmarshaller.unmarshal(element.getSource()));
			}
			return result;
		}
		else {
			SoapHeaderElement element = extractSoapHeader(qname, soapHeader);
			if (element != null) {
				Unmarshaller unmarshaller = this.unmarshaller;
				Assert.state(unmarshaller != null,
						"'unmarshaller' must be set to extract SOAP header with custom type");
				return unmarshaller.unmarshal(element.getSource());
			}
			return null;
		}
	}

	private @Nullable SoapHeaderElement extractSoapHeader(QName qname,
			org.springframework.ws.soap.SoapHeader soapHeader) {
		Iterator<SoapHeaderElement> elements = soapHeader.examineAllHeaderElements();
		while (elements.hasNext()) {
			SoapHeaderElement e = elements.next();
			if (e.getName().equals(qname)) {
				return e;
			}
		}
		return null;
	}

	private List<SoapHeaderElement> extractSoapHeaderList(QName qname,
			org.springframework.ws.soap.SoapHeader soapHeader) {
		List<SoapHeaderElement> result = new ArrayList<>();
		Iterator<SoapHeaderElement> elements = soapHeader.examineAllHeaderElements();
		while (elements.hasNext()) {
			SoapHeaderElement e = elements.next();
			if (e.getName().equals(qname)) {
				result.add(e);
			}
		}
		return result;
	}

}
