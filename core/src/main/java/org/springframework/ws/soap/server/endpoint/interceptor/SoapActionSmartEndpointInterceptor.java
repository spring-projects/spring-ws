/*
 * Copyright 2005-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.soap.server.endpoint.interceptor;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.SoapMessage;

/**
 * Implementation of the {@link org.springframework.ws.server.SmartEndpointInterceptor} interface that only intercepts
 * requests that have a specified soap action.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public class SoapActionSmartEndpointInterceptor extends DelegatingSmartSoapEndpointInterceptor {

    private final String soapAction;

    public SoapActionSmartEndpointInterceptor(EndpointInterceptor delegate, String soapAction) {
        super(delegate);
        Assert.hasLength(soapAction, "soapAction can not be empty");
        this.soapAction = soapAction;
    }

    @Override
    protected boolean shouldIntercept(WebServiceMessage request, Object endpoint) {
        if (request instanceof SoapMessage) {
            String soapAction = ((SoapMessage) request).getSoapAction();
            if (StringUtils.hasLength(soapAction) && soapAction.charAt(0) == '"' &&
                    soapAction.charAt(soapAction.length() - 1) == '"') {
                soapAction = soapAction.substring(1, soapAction.length() - 1);
            }
            return this.soapAction.equals(soapAction);
        }
        else {
            return false;
        }
    }
}
