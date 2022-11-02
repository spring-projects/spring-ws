/*
 * Copyright 2005-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.server.endpoint.mapping;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerFactory;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointMapping;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.PayloadRoots;
import org.springframework.ws.server.endpoint.support.PayloadRootUtils;
import org.springframework.xml.transform.TransformerFactoryUtils;

/**
 * Implementation of the {@link EndpointMapping} interface that uses the {@link PayloadRoot} annotation to map methods
 * to request payload root elements.
 * <p>
 * Endpoints typically have the following form:
 *
 * <pre>
 * &#64;Endpoint
 * public class MyEndpoint{
 *	  &#64;PayloadRoot(localPart = "Request",
 *				   namespace = "http://springframework.org/spring-ws")
 *	  public Source doSomethingWithRequest() {
 *		 ...
 *	  }
 * }
 * </pre>
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public class PayloadRootAnnotationMethodEndpointMapping extends AbstractAnnotationMethodEndpointMapping<QName> {

	private static TransformerFactory transformerFactory;

	static {
		setTransformerFactory(TransformerFactoryUtils.newInstance());
	}

	/**
	 * Override the default {@link TransformerFactory}.
	 *
	 * @param transformerFactory
	 */
	public static void setTransformerFactory(TransformerFactory transformerFactory) {
		PayloadRootAnnotationMethodEndpointMapping.transformerFactory = transformerFactory;
	}

	@Override
	protected QName getLookupKeyForMessage(MessageContext messageContext) throws Exception {
		return PayloadRootUtils.getPayloadRootQName(messageContext.getRequest().getPayloadSource(), transformerFactory);
	}

	@Override
	protected List<QName> getLookupKeysForMethod(Method method) {
		List<QName> result = new ArrayList<QName>();

		PayloadRoots payloadRoots = AnnotationUtils.findAnnotation(method, PayloadRoots.class);
		if (payloadRoots != null) {
			for (PayloadRoot payloadRoot : payloadRoots.value()) {
				result.add(getQNameFromAnnotation(payloadRoot));
			}
		} else {
			PayloadRoot payloadRoot = AnnotationUtils.findAnnotation(method, PayloadRoot.class);
			if (payloadRoot != null) {
				result.add(getQNameFromAnnotation(payloadRoot));
			}
		}

		return result;
	}

	private QName getQNameFromAnnotation(PayloadRoot payloadRoot) {
		if (StringUtils.hasLength(payloadRoot.localPart()) && StringUtils.hasLength(payloadRoot.namespace())) {
			return new QName(payloadRoot.namespace(), payloadRoot.localPart());
		} else {
			return new QName(payloadRoot.localPart());
		}
	}

}
