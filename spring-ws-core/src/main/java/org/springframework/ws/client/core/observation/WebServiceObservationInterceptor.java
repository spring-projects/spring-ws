/*
 * Copyright 2005-2024 the original author or authors.
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
package org.springframework.ws.client.core.observation;

import io.micrometer.common.util.internal.logging.WarnThenDebugLogger;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.ws.FaultAwareWebServiceMessage;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.support.interceptor.ClientInterceptorAdapter;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.support.ObservationHelper;
import org.springframework.ws.transport.HeadersAwareSenderWebServiceConnection;
import org.springframework.ws.transport.TransportConstants;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Interceptor that creates an Observation for each operation.
 *
 * @author Johan Kindgren
 * @see Observation
 * @see io.micrometer.observation.ObservationConvention
 */
public class WebServiceObservationInterceptor extends ClientInterceptorAdapter {

    private static final WarnThenDebugLogger WARN_THEN_DEBUG_LOGGER = new WarnThenDebugLogger(WebServiceObservationInterceptor.class);
    private static final String OBSERVATION_KEY = "observation";
    private static final WebServiceTemplateConvention DEFAULT_CONVENTION = new DefaultWebServiceTemplateConvention();

    private final ObservationRegistry observationRegistry;

    private final WebServiceTemplateConvention customConvention;
    private final ObservationHelper observationHelper;

    public WebServiceObservationInterceptor(
            @NonNull
            ObservationRegistry observationRegistry,
            @NonNull
            ObservationHelper observationHelper,
            @Nullable
            WebServiceTemplateConvention customConvention) {

        this.observationRegistry = observationRegistry;
        this.observationHelper = observationHelper;
        this.customConvention = customConvention;
    }


    @Override
    public boolean handleRequest(MessageContext messageContext) throws WebServiceClientException {

        TransportContext transportContext = TransportContextHolder.getTransportContext();
        HeadersAwareSenderWebServiceConnection connection =
                (HeadersAwareSenderWebServiceConnection) transportContext.getConnection();

        Observation observation = WebServiceTemplateObservationDocumentation.WEB_SERVICE_TEMPLATE.start(
                customConvention,
                DEFAULT_CONVENTION,
                () -> new WebServiceTemplateObservationContext(connection),
                observationRegistry);

        messageContext.setProperty(OBSERVATION_KEY, observation);

        return true;
    }

    @Override
    public void afterCompletion(MessageContext messageContext, Exception ex) {

        Observation observation = (Observation) messageContext.getProperty(OBSERVATION_KEY);
        if (observation == null) {
            WARN_THEN_DEBUG_LOGGER.log("Missing expected Observation in messageContext; the request will not be observed.");
            return;
        }

        WebServiceTemplateObservationContext context = (WebServiceTemplateObservationContext) observation.getContext();

        WebServiceMessage request = messageContext.getRequest();
        WebServiceMessage response = messageContext.getResponse();

        if (request instanceof SoapMessage soapMessage) {

            Source source = soapMessage.getSoapBody().getPayloadSource();
            QName root = observationHelper.getRootElement(source);
            if (root != null) {
                context.setLocalPart(root.getLocalPart());
                context.setNamespace(root.getNamespaceURI());
            }
            if (soapMessage.getSoapAction() != null && !soapMessage.getSoapAction().equals(TransportConstants.EMPTY_SOAP_ACTION)) {
                context.setSoapAction(soapMessage.getSoapAction());
            }
        }

        if (ex == null) {
            context.setOutcome("success");
        } else {
            context.setError(ex);
            context.setOutcome("fault");
        }

        if (response instanceof FaultAwareWebServiceMessage faultAwareResponse) {
            if (faultAwareResponse.hasFault()) {
                context.setOutcome("fault");
            }
        }

        URI uri = getUriFromConnection();
        if (uri != null) {
            context.setHost(uri.getHost());
            context.setPath(uri.getPath());
        }

        context.setContextualName("POST");

        observation.stop();
    }

    URI getUriFromConnection() {
        TransportContext transportContext = TransportContextHolder.getTransportContext();
        WebServiceConnection connection = transportContext.getConnection();
        try {
            return connection.getUri();
        } catch (URISyntaxException e) {
            return null;
        }
    }
}

