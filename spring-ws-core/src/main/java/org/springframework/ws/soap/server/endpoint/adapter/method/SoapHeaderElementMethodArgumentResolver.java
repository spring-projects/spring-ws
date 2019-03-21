/*
 * Copyright 2005-2014 the original author or authors.
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

import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.adapter.method.MethodArgumentResolver;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.server.endpoint.annotation.SoapHeader;
import org.springframework.xml.namespace.QNameUtils;

/**
 * Implementation of {@link MethodArgumentResolver} that supports resolving {@link SoapHeaderElement} parameters. Target
 * method parameters must be annotated with {@link SoapHeader} to indicate the SOAP header to resolve. This resolver
 * supports simple {@link SoapHeaderElement} parameters and {@link List} parameters for elements that appear multiple
 * times in the same SOAP header. </p> The following snippet shows an example of supported declarations.
 * <pre><code>
 * public void soapHeaderElement(@SoapHeader("{http://springframework.org/ws}header") SoapHeaderElement element)
 *
 * public void soapHeaderElementList(@SoapHeader("{http://springframework.org/ws}header") List&lt;SoapHeaderElement&gt; elements)
 * </code></pre>
 *
 * @author Tareq Abedrabbo
 * @author Arjen Poutsma
 * @see SoapHeader
 * @since 2.0
 */
public class SoapHeaderElementMethodArgumentResolver implements MethodArgumentResolver {

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

		// List<SoapHeaderElement> parameter
		if (List.class.equals(parameterType)) {
			Type genericType = parameter.getGenericParameterType();
			if (genericType instanceof ParameterizedType) {
				ParameterizedType parameterizedType = (ParameterizedType) genericType;
				Type[] typeArguments = parameterizedType.getActualTypeArguments();
				if (typeArguments.length == 1 && SoapHeaderElement.class.equals(typeArguments[0])) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Object resolveArgument(MessageContext messageContext, MethodParameter parameter) throws Exception {
		Assert.isInstanceOf(SoapMessage.class, messageContext.getRequest());
		SoapMessage request = (SoapMessage) messageContext.getRequest();
		org.springframework.ws.soap.SoapHeader soapHeader = request.getSoapHeader();

		String paramValue = parameter.getParameterAnnotation(SoapHeader.class).value();

		Assert.isTrue(QNameUtils.validateQName(paramValue), "Invalid header qualified name [" + paramValue + "]. " +
				"QName must be of the form '{namespace}localPart'.");

		QName qname = QName.valueOf(paramValue);

		Class<?> parameterType = parameter.getParameterType();

		if (SoapHeaderElement.class.equals(parameterType)) {
			return extractSoapHeader(qname, soapHeader);
		}
		else if (List.class.equals(parameterType)) {
			return extractSoapHeaderList(qname, soapHeader);
		}
		// should not happen
		throw new UnsupportedOperationException();
	}

	private SoapHeaderElement extractSoapHeader(QName qname, org.springframework.ws.soap.SoapHeader soapHeader) {
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
		List<SoapHeaderElement> result = new ArrayList<SoapHeaderElement>();
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
