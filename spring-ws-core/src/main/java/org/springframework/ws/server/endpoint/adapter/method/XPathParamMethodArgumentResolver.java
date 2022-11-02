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

package org.springframework.ws.server.endpoint.adapter.method;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.ConversionServiceFactory;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.annotation.XPathParam;
import org.springframework.ws.server.endpoint.support.NamespaceUtils;
import org.springframework.xml.transform.TransformerHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Implementation of {@link MethodArgumentResolver} that supports the {@link XPathParam @XPathParam} annotation.
 * <p>
 * This resolver supports parameters annotated with {@link XPathParam @XPathParam} that specifies the XPath expression
 * that should be bound to that parameter. The parameter can either a "natively supported" XPath type ({@link Boolean
 * boolean}, {@link Double double}, {@link String}, {@link Node}, or {@link NodeList}), or a type that is
 * {@linkplain ConversionService#canConvert(Class, Class) supported} by the {@link ConversionService}.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public class XPathParamMethodArgumentResolver implements MethodArgumentResolver {

	private final XPathFactory xpathFactory = createXPathFactory();

	private TransformerHelper transformerHelper = new TransformerHelper();

	private ConversionService conversionService = new DefaultConversionService();

	/**
	 * Sets the conversion service to use.
	 * <p>
	 * Defaults to the {@linkplain ConversionServiceFactory#createDefaultConversionService() default conversion service}.
	 */
	public void setConversionService(ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	public void setTransformerHelper(TransformerHelper transformerHelper) {
		this.transformerHelper = transformerHelper;
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		if (parameter.getParameterAnnotation(XPathParam.class) == null) {
			return false;
		}
		Class<?> parameterType = parameter.getParameterType();
		if (Boolean.class.equals(parameterType) || Boolean.TYPE.equals(parameterType) || Double.class.equals(parameterType)
				|| Double.TYPE.equals(parameterType) || Node.class.isAssignableFrom(parameterType)
				|| NodeList.class.isAssignableFrom(parameterType) || String.class.isAssignableFrom(parameterType)) {
			return true;
		} else {
			return conversionService.canConvert(String.class, parameterType);
		}
	}

	@Override
	public Object resolveArgument(MessageContext messageContext, MethodParameter parameter)
			throws TransformerException, XPathExpressionException {
		Class<?> parameterType = parameter.getParameterType();
		QName evaluationReturnType = getReturnType(parameterType);
		boolean useConversionService = false;
		if (evaluationReturnType == null) {
			evaluationReturnType = XPathConstants.STRING;
			useConversionService = true;
		}

		XPath xpath = createXPath();
		xpath.setNamespaceContext(NamespaceUtils.getNamespaceContext(parameter.getMethod()));

		Element rootElement = getRootElement(messageContext.getRequest().getPayloadSource());
		String expression = parameter.getParameterAnnotation(XPathParam.class).value();
		Object result = xpath.evaluate(expression, rootElement, evaluationReturnType);
		return useConversionService ? conversionService.convert(result, parameterType) : result;
	}

	private QName getReturnType(Class<?> parameterType) {
		if (Boolean.class.equals(parameterType) || Boolean.TYPE.equals(parameterType)) {
			return XPathConstants.BOOLEAN;
		} else if (Double.class.equals(parameterType) || Double.TYPE.equals(parameterType)) {
			return XPathConstants.NUMBER;
		} else if (Node.class.equals(parameterType)) {
			return XPathConstants.NODE;
		} else if (NodeList.class.equals(parameterType)) {
			return XPathConstants.NODESET;
		} else if (String.class.equals(parameterType)) {
			return XPathConstants.STRING;
		} else {
			return null;
		}
	}

	private XPath createXPath() {
		synchronized (xpathFactory) {
			return xpathFactory.newXPath();
		}
	}

	private Element getRootElement(Source source) throws TransformerException {
		DOMResult domResult = new DOMResult();
		transformerHelper.transform(source, domResult);
		Document document = (Document) domResult.getNode();
		return document.getDocumentElement();
	}

	/**
	 * Create a {@code XPathFactory} that this resolver will use to create {@link XPath} objects.
	 * <p>
	 * Can be overridden in subclasses, adding further initialization of the factory. The resulting factory is cached, so
	 * this method will only be called once.
	 *
	 * @return the created factory
	 */
	protected XPathFactory createXPathFactory() {
		return XPathFactory.newInstance();
	}

}
