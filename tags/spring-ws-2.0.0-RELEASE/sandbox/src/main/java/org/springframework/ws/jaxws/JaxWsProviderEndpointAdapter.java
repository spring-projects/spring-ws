/*
 * Copyright 2007 the original author or authors.
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

package org.springframework.ws.jaxws;

import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointAdapter;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.xml.transform.TransformerObjectSupport;

/**
 * Adapter to use a JAX-WS {@link Provider} as the endpoint for a <code>EndpointInvocationChain</code>. Supports both
 * message and payload providers.
 *
 * @author Arjen Poutsma
 */
public class JaxWsProviderEndpointAdapter extends TransformerObjectSupport implements EndpointAdapter {

    public boolean supports(Object endpoint) {
        return endpoint.getClass().getAnnotation(WebServiceProvider.class) != null && endpoint instanceof Provider;
    }

    public void invoke(MessageContext messageContext, Object endpoint) throws Exception {
        ServiceMode serviceMode = endpoint.getClass().getAnnotation(ServiceMode.class);
        if (serviceMode == null || Service.Mode.PAYLOAD.equals(serviceMode.value())) {
            invokeSourceProvider(messageContext, (Provider<Source>) endpoint);
        }
        else if (Service.Mode.MESSAGE.equals(serviceMode.value())) {
            Provider<SOAPMessage> provider = (Provider<SOAPMessage>) endpoint;
            invokeMessageProvider(messageContext, provider);
        }
    }

    private void invokeSourceProvider(MessageContext messageContext, Provider<Source> provider)
            throws TransformerException {
        Source requestSource = messageContext.getRequest().getPayloadSource();
        Source responseSource = provider.invoke(requestSource);
        if (responseSource != null) {
            WebServiceMessage response = messageContext.getResponse();
            Transformer transformer = createTransformer();
            transformer.transform(responseSource, response.getPayloadResult());
        }
    }

    private void invokeMessageProvider(MessageContext messageContext, Provider<SOAPMessage> provider) {
        if (!(messageContext.getRequest() instanceof SaajSoapMessage)) {
            throw new IllegalArgumentException("JaxWsProviderEndpointAdapter requires a SaajSoapMessage. " +
                    "Use a SaajSoapMessageFactory to create the SOAP messages.");
        }
        SaajSoapMessage request = (SaajSoapMessage) messageContext.getRequest();
        SOAPMessage saajRequest = request.getSaajMessage();
        SOAPMessage saajResponse = provider.invoke(saajRequest);
        if (saajResponse != null) {
            SaajSoapMessage response = (SaajSoapMessage) messageContext.getResponse();
            response.setSaajMessage(saajResponse);
        }
    }
}
