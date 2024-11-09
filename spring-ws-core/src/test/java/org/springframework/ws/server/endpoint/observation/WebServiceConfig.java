package org.springframework.ws.server.endpoint.observation;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.tck.TestObservationRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.context.TestPropertySource;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.server.EndpointInterceptor;

import java.util.List;

@EnableWs
@Configuration
@TestPropertySource(properties = "management.tracing.sampling.probability=1")
public class WebServiceConfig extends WsConfigurerAdapter {

    @Autowired
    private ObservationRegistry observationRegistry;

    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(MyEndpoint.MyRequest.class, MyEndpoint.MyResponse.class);
        return marshaller;
    }

    @Bean
    public ObservationRegistry observationRegistry() {
        TestObservationRegistry registry = TestObservationRegistry.create();

        registry.observationConfig();
        return registry;
    }

    @Bean
    public WebServiceTemplate webServiceTemplate(Jaxb2Marshaller marshaller) {
        WebServiceTemplate webServiceTemplate = new WebServiceTemplate();
        webServiceTemplate.setMarshaller(marshaller);
        webServiceTemplate.setUnmarshaller(marshaller);
        return webServiceTemplate;
    }

    @Bean
    public EndpointInterceptor observationInterceptor() {
        return new ObservationInterceptor(observationRegistry); // Replace with your actual interceptor
    }


    @Override
    public void addInterceptors(List<EndpointInterceptor> interceptors) {
        interceptors.add(observationInterceptor());
    }

}