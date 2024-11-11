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
package org.springframework.ws.server.endpoint.observation;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.util.Assert;
import org.springframework.ws.FaultAwareWebServiceMessage;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.observation.RootElementSAXHandler;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.interceptor.EndpointInterceptorAdapter;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.transport.HeadersAwareReceiverWebServiceConnection;
import org.springframework.ws.transport.TransportConstants;
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
/**
 * Interceptor implementation that creates an observation for a WebService Endpoint.
 * @author Johan Kindgren
 */
public class ObservationInterceptor extends EndpointInterceptorAdapter {

    public static final String OBSERVATION_KEY = "observation";
    private ObservationRegistry observationRegistry;

    private static final WebServiceEndpointConvention DEFAULT_CONVENTION = new DefaultWebServiceEndpointConvention();

    private SAXParserFactory parserFactory;
    private SAXParser saxParser;

    private WebServiceEndpointConvention customConvention;

    public ObservationInterceptor(ObservationRegistry observationRegistry) {
        this.observationRegistry = observationRegistry;

        parserFactory = SAXParserFactory.newNSInstance();
        try {
            saxParser = parserFactory.newSAXParser();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean handleRequest(MessageContext messageContext, Object endpoint) throws Exception {

        TransportContext transportContext = TransportContextHolder.getTransportContext();
        HeadersAwareReceiverWebServiceConnection connection =
                (HeadersAwareReceiverWebServiceConnection) transportContext.getConnection();


        Observation observation = EndpointObservationDocumentation.WEB_SERVICE_ENDPOINT.start(
                customConvention,
                DEFAULT_CONVENTION,
                () -> new WebServiceEndpointContext(connection),
                observationRegistry);

        messageContext.setProperty(OBSERVATION_KEY, observation);

        return true;
    }

    @Override
    public void afterCompletion(MessageContext messageContext, Object endpoint, Exception ex) throws Exception {

        Observation observation = (Observation) messageContext.getProperty(OBSERVATION_KEY);
        Assert.notNull(observation, "Expected observation in messageContext");

        WebServiceEndpointContext context = (WebServiceEndpointContext) observation.getContext();

        context.setError(ex);

        WebServiceMessage request = messageContext.getRequest();
        WebServiceMessage response = messageContext.getResponse();

        if (request instanceof SoapMessage soapMessage) {

            Source source = soapMessage.getSoapBody().getPayloadSource();
            QName root = getRootElement(source);
            if (root != null) {
                context.setLocalPart(root.getLocalPart());
                context.setNamespace(root.getNamespaceURI());
            }
            String action = soapMessage.getSoapAction();
            if (!TransportConstants.EMPTY_SOAP_ACTION.equals(action)) {
                context.setSoapAction(soapMessage.getSoapAction());
            } else {
                context.setSoapAction("none");
            }

        }

        if (response instanceof FaultAwareWebServiceMessage) {
            if (!((FaultAwareWebServiceMessage) response).hasFault() && ex == null) {
                context.setOutcome("success");
            } else {
                context.setOutcome("fault");
            }
        }
        StringBuilder contextualName = new StringBuilder("WebServiceEndpoint ")
                .append(context.getNamespace()).append(":")
                .append(context.getLocalPart());
        context.setContextualName(contextualName.toString());

        observation.stop();
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

    public void setCustomConvention(WebServiceEndpointConvention customConvention) {
        this.customConvention = customConvention;
    }
}
