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
import org.springframework.ws.FaultAwareWebServiceMessage;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.support.interceptor.ClientInterceptorAdapter;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.transport.HeadersAwareSenderWebServiceConnection;
import org.springframework.ws.transport.TransportConstants;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
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
    private final SAXParserFactory parserFactory;
    private final SAXParser saxParser;

    private final WebServiceTemplateConvention customConvention;

    public WebServiceObservationInterceptor(
            ObservationRegistry observationRegistry,
            WebServiceTemplateConvention customConvention) {
        this.observationRegistry = observationRegistry;
        this.customConvention = customConvention;
        parserFactory = SAXParserFactory.newNSInstance();
        try {
            saxParser = parserFactory.newSAXParser();
        } catch (ParserConfigurationException | SAXException e) {
            throw new RuntimeException(e);
        }
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
            QName root = getRootElement(source);
            if (root != null) {
                context.setLocalPart(root.getLocalPart());
                context.setNamespace(root.getNamespaceURI());
            }
            if (soapMessage.getSoapAction() != null && !soapMessage.getSoapAction().equals(TransportConstants.EMPTY_SOAP_ACTION)) {
                context.setSoapAction(soapMessage.getSoapAction());
            }
        }

        if (response instanceof FaultAwareWebServiceMessage faultAwareResponse) {
            if (!faultAwareResponse.hasFault() && ex == null) {
                context.setOutcome("success");
            } else {
                context.setOutcome("fault");
            }
        }

        URI uri = getUriFromConnection();
        if (uri != null) {
            context.setHost(uri.getHost());
            context.setPath(uri.getPath());
        }

        context.setContextualName("POST");

        context.setError(ex);

        observation.stop();
    }

    URI getUriFromConnection()  {
        TransportContext transportContext = TransportContextHolder.getTransportContext();
        WebServiceConnection connection = transportContext.getConnection();
        try {
            return connection.getUri();
        } catch (URISyntaxException e) {
            return null;
        }
    }

    QName getRootElement(Source source) {
        if (source instanceof DOMSource) {
            Node root = ((DOMSource) source).getNode();
            return new QName(root.getNamespaceURI(), root.getLocalName());
        }
        if (source instanceof StreamSource) {
            RootElementSAXHandler handler = new RootElementSAXHandler();
            try {
                saxParser.parse(((StreamSource) source).getInputStream(), handler);
                return handler.getRootElementName();
            } catch (Exception e) {
                return new QName("unknown", "unknow");
            }
        }
        if (source instanceof SAXSource) {
            RootElementSAXHandler handler = new RootElementSAXHandler();
            try {
                saxParser.parse(((SAXSource) source).getInputSource(), handler);
                return handler.getRootElementName();
            } catch (Exception e) {
                return new QName("unknown", "unknow");
            }
        }
        return new QName("unknown", "unknow");
    }

}

