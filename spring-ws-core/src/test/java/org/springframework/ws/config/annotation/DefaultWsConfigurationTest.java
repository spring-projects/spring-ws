package org.springframework.ws.config.annotation;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ws.server.endpoint.adapter.DefaultMethodEndpointAdapter;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.mapping.PayloadRootAnnotationMethodEndpointMapping;
import org.springframework.ws.soap.addressing.server.AnnotationActionEndpointMapping;
import org.springframework.ws.soap.server.endpoint.annotation.SoapAction;
import org.springframework.ws.soap.server.endpoint.mapping.SoapActionAnnotationMethodEndpointMapping;

/**
 * @author Arjen Poutsma
 */
public class DefaultWsConfigurationTest {

	private ApplicationContext applicationContext;

	@BeforeEach
	public void setUp() throws Exception {

		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
		applicationContext.register(TestConfig.class);
		applicationContext.refresh();

		this.applicationContext = applicationContext;
	}

	@Test
	public void payloadRootAnnotationMethodEndpointMapping() {

		PayloadRootAnnotationMethodEndpointMapping endpointMapping = this.applicationContext
				.getBean(PayloadRootAnnotationMethodEndpointMapping.class);

		assertThat(endpointMapping.getOrder()).isEqualTo(0);
	}

	@Test
	public void soapActionAnnotationMethodEndpointMapping() {

		SoapActionAnnotationMethodEndpointMapping endpointMapping = this.applicationContext
				.getBean(SoapActionAnnotationMethodEndpointMapping.class);

		assertThat(endpointMapping.getOrder()).isEqualTo(1);
	}

	@Test
	public void annotationActionEndpointMapping() {

		AnnotationActionEndpointMapping endpointMapping = this.applicationContext
				.getBean(AnnotationActionEndpointMapping.class);

		assertThat(endpointMapping.getOrder()).isEqualTo(2);
	}

	@Test
	public void defaultMethodEndpointAdapter() {

		DefaultMethodEndpointAdapter adapter = this.applicationContext.getBean(DefaultMethodEndpointAdapter.class);

		assertThat(adapter.getMethodArgumentResolvers()).isNotEmpty();
		assertThat(adapter.getMethodReturnValueHandlers()).isNotEmpty();
	}

	@EnableWs
	@Configuration
	public static class TestConfig {

		@Bean(name = "testEndpoint")
		public TestEndpoint testEndpoint() {
			return new TestEndpoint();
		}
	}

	@Endpoint
	private static class TestEndpoint {

		@SoapAction("handle")
		public void handle() {}

	}

}
