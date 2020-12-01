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

package org.springframework.ws.server.endpoint.adapter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MethodEndpoint;
import org.springframework.ws.server.endpoint.annotation.XPathParam;
import org.springframework.xml.namespace.SimpleNamespaceContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Adapter that supports endpoint methods that use XPath expressions. Supports methods with the following signature:
 * 
 * <pre>
 * void handleMyMessage(@XPathParam("/root/child/text") String param);
 * </pre>
 * 
 * or
 * 
 * <pre>
 * Source handleMyMessage(@XPathParam("/root/child/text") String param1,
 * 		&#64;XPathParam("/root/child/number") double param2);
 * </pre>
 * 
 * I.e. methods that return either {@code void} or a {@link Source}, and have parameters annotated with
 * {@link XPathParam} that specify the XPath expression that should be bound to that parameter. The parameter can be of
 * the following types:
 * <ul>
 * <li>{@code boolean}, or {@link Boolean}</li>
 * <li>{@code double}, or {@link Double}</li>
 * <li>{@link String}</li>
 * <li>{@link Node}</li>
 * <li>{@link NodeList}</li>
 * </ul>
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @deprecated as of Spring Web Services 2.0, in favor of {@link DefaultMethodEndpointAdapter} and
 *             {@link org.springframework.ws.server.endpoint.adapter.method.XPathParamMethodArgumentResolver
 *             XPathParamMethodArgumentResolver}.
 */
@Deprecated
public class XPathParamAnnotationMethodEndpointAdapter extends AbstractMethodEndpointAdapter
		implements InitializingBean {

	private XPathFactory xpathFactory;

	private Map<String, String> namespaces;

	/** Sets namespaces used in the XPath expression. Maps prefixes to namespaces. */
	public void setNamespaces(Map<String, String> namespaces) {
		this.namespaces = namespaces;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		xpathFactory = XPathFactory.newInstance();
	}

	/** Supports methods with @XPathParam parameters, and return either {@code Source} or nothing. */
	@Override
	protected boolean supportsInternal(MethodEndpoint methodEndpoint) {
		Method method = methodEndpoint.getMethod();
		if (!(Source.class.isAssignableFrom(method.getReturnType()) || Void.TYPE.equals(method.getReturnType()))) {
			return false;
		}
		Class<?>[] parameterTypes = method.getParameterTypes();
		for (int i = 0; i < parameterTypes.length; i++) {
			if (getXPathParamAnnotation(method, i) == null || !isSupportedType(parameterTypes[i])) {
				return false;
			}
		}
		return true;
	}

	private XPathParam getXPathParamAnnotation(Method method, int paramIdx) {
		Annotation[][] paramAnnotations = method.getParameterAnnotations();
		for (int annIdx = 0; annIdx < paramAnnotations[paramIdx].length; annIdx++) {
			if (paramAnnotations[paramIdx][annIdx].annotationType().equals(XPathParam.class)) {
				return (XPathParam) paramAnnotations[paramIdx][annIdx];
			}
		}
		return null;
	}

	private boolean isSupportedType(Class<?> clazz) {
		return Boolean.class.isAssignableFrom(clazz) || Boolean.TYPE.isAssignableFrom(clazz)
				|| Double.class.isAssignableFrom(clazz) || Double.TYPE.isAssignableFrom(clazz)
				|| Node.class.isAssignableFrom(clazz) || NodeList.class.isAssignableFrom(clazz)
				|| String.class.isAssignableFrom(clazz);
	}

	@Override
	protected void invokeInternal(MessageContext messageContext, MethodEndpoint methodEndpoint) throws Exception {
		Element payloadElement = getRootElement(messageContext.getRequest().getPayloadSource());
		Object[] args = getMethodArguments(payloadElement, methodEndpoint.getMethod());
		Object result = methodEndpoint.invoke(args);
		if (result != null && result instanceof Source) {
			Source responseSource = (Source) result;
			WebServiceMessage response = messageContext.getResponse();
			transform(responseSource, response.getPayloadResult());
		}
	}

	private Object[] getMethodArguments(Element payloadElement, Method method) throws XPathExpressionException {
		Class<?>[] parameterTypes = method.getParameterTypes();
		XPath xpath = createXPath();
		Object[] args = new Object[parameterTypes.length];
		for (int i = 0; i < parameterTypes.length; i++) {
			String expression = getXPathParamAnnotation(method, i).value();
			QName conversionType;
			if (Boolean.class.isAssignableFrom(parameterTypes[i]) || Boolean.TYPE.isAssignableFrom(parameterTypes[i])) {
				conversionType = XPathConstants.BOOLEAN;
			} else if (Double.class.isAssignableFrom(parameterTypes[i]) || Double.TYPE.isAssignableFrom(parameterTypes[i])) {
				conversionType = XPathConstants.NUMBER;
			} else if (Node.class.isAssignableFrom(parameterTypes[i])) {
				conversionType = XPathConstants.NODE;
			} else if (NodeList.class.isAssignableFrom(parameterTypes[i])) {
				conversionType = XPathConstants.NODESET;
			} else if (String.class.isAssignableFrom(parameterTypes[i])) {
				conversionType = XPathConstants.STRING;
			} else {
				throw new IllegalArgumentException("Invalid parameter type [" + parameterTypes[i] + "]. "
						+ "Supported are: Boolean, Double, Node, NodeList, and String.");
			}
			args[i] = xpath.evaluate(expression, payloadElement, conversionType);
		}
		return args;
	}

	private synchronized XPath createXPath() {
		XPath xpath = xpathFactory.newXPath();
		if (namespaces != null) {
			SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
			namespaceContext.setBindings(namespaces);
			xpath.setNamespaceContext(namespaceContext);
		}
		return xpath;
	}

	/**
	 * Returns the root element of the given source.
	 *
	 * @param source the source to get the root element from
	 * @return the root element
	 */
	private Element getRootElement(Source source) throws TransformerException {
		DOMResult domResult = new DOMResult();
		transform(source, domResult);
		Document document = (Document) domResult.getNode();
		return document.getDocumentElement();
	}

}
