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

import io.micrometer.observation.tck.TestObservationRegistry;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.jupiter.api.*;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.transport.HeadersAwareSenderWebServiceConnection;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.transport.support.FreePortScanner;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import java.io.IOException;

import static io.micrometer.observation.tck.TestObservationRegistryAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Verifies observation for a WebService Endpoint.
 * @author Johan Kindgren
 */
public class ObservationInterceptorIntegrationTest {

    private static AnnotationConfigWebApplicationContext applicationContext;
    private WebServiceTemplate webServiceTemplate;
    private TestObservationRegistry registry;
    private static Server server;

    private final String requestPayload = "<root xmlns='http://springframework.org/spring-ws'><child/></root>";

    private TransformerFactory transformerFactory = TransformerFactory.newInstance();
    private Transformer transformer;

    private static String baseUrl;

    @BeforeAll
    public static void startServer() throws Exception {

        int port = FreePortScanner.getFreePort();

        baseUrl = "http://localhost:" + port + "/ws";

        server = new Server(port);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        applicationContext = new AnnotationConfigWebApplicationContext();
        applicationContext.scan(WebServiceConfig.class.getPackage().getName());

        MessageDispatcherServlet dispatcherServlet = new MessageDispatcherServlet(applicationContext);
        dispatcherServlet.setTransformWsdlLocations(true);



        ServletHolder servletHolder = new ServletHolder(dispatcherServlet);
        context.addServlet(servletHolder, "/ws/*");

        server.setHandler(context);
        server.start();
    }

    @AfterAll
    public static void stopServer() throws Exception {
        server.stop();
    }

    @BeforeEach
    void setUp() throws TransformerConfigurationException {


        webServiceTemplate = applicationContext.getBean(WebServiceTemplate.class);
        registry = applicationContext.getBean(TestObservationRegistry.class);

        transformer = transformerFactory.newTransformer();
    }

    @Test
    void testObservationInterceptorBehavior() {

        MyEndpoint.MyRequest request = new MyEndpoint.MyRequest();
        request.setName("John");
        MyEndpoint.MyResponse response = (MyEndpoint.MyResponse) webServiceTemplate.marshalSendAndReceive(baseUrl, request, new WebServiceMessageCallback() {
            @Override
            public void doWithMessage(WebServiceMessage message) throws IOException, TransformerException {
                TransportContext transportContext = TransportContextHolder.getTransportContext();
                HeadersAwareSenderWebServiceConnection connection =
                        (HeadersAwareSenderWebServiceConnection) transportContext.getConnection();
                connection.addRequestHeader("traceparent", "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01");
            }
        });

        // Assertions based on expected behavior of ObservationInterceptor
        assertNotNull(response);

        assertThat(registry).hasAnObservation(observationContextAssert ->
                observationContextAssert
                        .hasLowCardinalityKeyValue("outcome", "success")
                        .hasLowCardinalityKeyValue("exception", "none")
                        .hasLowCardinalityKeyValue("namespace", "http://springframework.org/spring-ws")
                        .hasLowCardinalityKeyValue("localname", "request")
                        .hasLowCardinalityKeyValue("soapaction", "none")
                        .hasContextualNameEqualTo("WebServiceEndpoint http://springframework.org/spring-ws:request")
                        .hasNameEqualTo("webservice.server")
        );
    }

    @AfterEach
    void tearDown() {
        applicationContext.close();
    }
}