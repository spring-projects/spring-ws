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

package org.springframework.ws.server.endpoint.mapping.jaxb;

import java.lang.reflect.Method;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.mapping.AbstractAnnotationMethodEndpointMapping;
import org.springframework.ws.server.endpoint.support.PayloadRootUtils;
import org.springframework.xml.transform.TransformerHelper;

/**
 * Implementation of the {@link org.springframework.ws.server.EndpointMapping EndpointMapping} interface that uses the
 * JAXB2 {@link XmlRootElement} annotation to map methods to request payload root elements.
 *
 * <p>Endpoints typically have the following form:
 * <pre>
 * &#64;Endpoint
 * public class MyEndpoint{
 *	  public void doSomethingWithRequest(&#64;RequestBody MyRootElement rootElement) {
 *		 ...
 *	  }
 * }
 * </pre>
 * where MyRootElement is annotated with {@code @XmlRootElement}:
 * <pre>
 * &#64;XmlRootElement(name = "myRoot", namespace = "http://springframework.org/spring-ws")
 * public class MyRootElement {
 *	 ...
 * }
 * </pre>
 *
 * @author Arjen Poutsma
 * @author Joost Koehoorn
 * @since 2.0
 */
public class XmlRootElementEndpointMapping extends AbstractAnnotationMethodEndpointMapping<QName> {

	private TransformerHelper transformerHelper = new TransformerHelper();

	private boolean requiresRequestBodyAnnotation = false;

	public void setTransformerHelper(TransformerHelper transformerHelper) {
		this.transformerHelper = transformerHelper;
	}

	public void setRequiresRequestBodyAnnotation(boolean requiresRequestBodyAnnotation) {
		this.requiresRequestBodyAnnotation = requiresRequestBodyAnnotation;
	}

	@Override
	protected QName getLookupKeyForMethod(Method method) {
		Class<?>[] parameterTypes = method.getParameterTypes();
		for (int i = 0; i < parameterTypes.length; i++) {
			MethodParameter methodParameter = new MethodParameter(method, i);
			if (parameterHasRequiredAnnotations(methodParameter)) {
				QName result = handleRootElement(methodParameter.getParameterType());
				if (result != null) {
					return result;
				}
			}

		}
		return null;
	}

	private boolean parameterHasRequiredAnnotations(MethodParameter parameter) {
		if (requiresRequestBodyAnnotation && !parameter.hasParameterAnnotation(RequestBody.class)) {
			return false;
		}

		return parameter.getParameterType().isAnnotationPresent(XmlRootElement.class);
	}

	private QName handleRootElement(Class<?> parameterType) {
		try {
			Object param = parameterType.newInstance();
			QName result = getElementName(parameterType, param);
			if (result != null) {
				return result;
			}
		}
		catch (InstantiationException e) {
			// ignore
		}
		catch (IllegalAccessException ex) {
			// ignore
		}
		return null;
	}

	private QName getElementName(Class<?> parameterType, Object param) {
		try {
			JAXBContext context = JAXBContext.newInstance(parameterType);
			JAXBIntrospector introspector = context.createJAXBIntrospector();
			return introspector.getElementName(param);
		}
		catch (JAXBException ex) {
			return null;
		}
	}

	@Override
	protected QName getLookupKeyForMessage(MessageContext messageContext) throws Exception {
		return PayloadRootUtils.getPayloadRootQName(messageContext.getRequest().getPayloadSource(), transformerHelper);
	}
}
