/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
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
 * Implementation of the
 * {@link org.springframework.ws.soap.server.SmartSoapEndpointInterceptor
 * SmartSoapEndpointInterceptor} interface that only intercepts requests that have a
 * specified namespace URI or local part (or both) as payload root.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public class PayloadRootSmartSoapEndpointInterceptor extends DelegatingSmartSoapEndpointInterceptor {

	private TransformerHelper transformerHelper = new TransformerHelper();

	private final String namespaceUri;

	private final String localPart;

	public PayloadRootSmartSoapEndpointInterceptor(EndpointInterceptor delegate, String namespaceUri,
			String localPart) {
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
			QName payloadRootName = PayloadRootUtils.getPayloadRootQName(request.getPayloadSource(),
					this.transformerHelper);
			if (payloadRootName == null || !this.namespaceUri.equals(payloadRootName.getNamespaceURI())) {
				return false;
			}
			return !StringUtils.hasLength(this.localPart) || this.localPart.equals(payloadRootName.getLocalPart());

		}
		catch (TransformerException ex) {
			return false;
		}
	}

}
