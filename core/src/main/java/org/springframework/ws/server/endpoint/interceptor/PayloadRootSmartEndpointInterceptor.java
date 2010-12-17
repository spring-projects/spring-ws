/*
 * Copyright 2005-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.server.endpoint.interceptor;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.endpoint.support.PayloadRootUtils;
import org.springframework.xml.transform.TransformerHelper;

/**
 * Implementation of the {@link org.springframework.ws.server.SmartEndpointInterceptor} interface that only intercepts
 * requests that have a specified namespace URI or local part (or both) as payload root.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public class PayloadRootSmartEndpointInterceptor extends DelegatingSmartEndpointInterceptor {

    private TransformerHelper transformerHelper = new TransformerHelper();

    private final String namespaceUri;

    private final String localPart;

    public PayloadRootSmartEndpointInterceptor(EndpointInterceptor delegate, String namespaceUri, String localPart) {
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
            return !(StringUtils.hasLength(namespaceUri) && !namespaceUri.equals(payloadRootName.getNamespaceURI()) ||
                    StringUtils.hasLength(localPart) && !localPart.equals(payloadRootName.getLocalPart()));

        }
        catch (TransformerException e) {
            return false;
        }
    }
}
