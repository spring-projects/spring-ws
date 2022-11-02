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

package org.springframework.ws.soap.server.endpoint.interceptor;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.endpoint.support.PayloadRootUtils;
import org.springframework.xml.transform.TransformerHelper;

/**
 * Implementation of the {@link org.springframework.ws.soap.server.SmartSoapEndpointInterceptor
 * SmartSoapEndpointInterceptor} interface that only intercepts requests that have a specified namespace URI or local
 * part (or both) as payload root.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public class PayloadRootSmartSoapEndpointInterceptor extends DelegatingSmartSoapEndpointInterceptor {

	private TransformerHelper transformerHelper = new TransformerHelper();

	private final String namespaceUri;

	private final String localPart;

	public PayloadRootSmartSoapEndpointInterceptor(EndpointInterceptor delegate, String namespaceUri, String localPart) {
		super(delegate);
		Assert.hasLength(namespaceUri, "namespaceUri can not be empty");
		this.namespaceUri = namespaceUri;
		this.localPart = localPart;
	}

	public void setTransformerHelper(TransformerHelper transformerHelper) {
		this.transformerHelper = transformerHelper;
	}

	@Override
	protected boolean shouldIntercept(WebServiceMessage request, Object endpoint) {
		try {
			QName payloadRootName = PayloadRootUtils.getPayloadRootQName(request.getPayloadSource(), transformerHelper);
			if (payloadRootName == null || !namespaceUri.equals(payloadRootName.getNamespaceURI())) {
				return false;
			}
			return !StringUtils.hasLength(localPart) || localPart.equals(payloadRootName.getLocalPart());

		} catch (TransformerException e) {
			return false;
		}
	}
}
