package org.springframework.ws.config.annotation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Before;
import org.junit.Test;

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

	@Before
	public void setUp() throws Exception {
		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
		applicationContext.register(TestConfig.class);
		applicationContext.refresh();

		this.applicationContext = applicationContext;
	}

	@Test
	public void payloadRootAnnotationMethodEndpointMapping() throws Exception {
		PayloadRootAnnotationMethodEndpointMapping endpointMapping = this.applicationContext.getBean(
				PayloadRootAnnotationMethodEndpointMapping.class);
		assertEquals(0, endpointMapping.getOrder());
	}

	@Test
	public void soapActionAnnotationMethodEndpointMapping() throws Exception {
		SoapActionAnnotationMethodEndpointMapping endpointMapping = this.applicationContext.getBean(
				SoapActionAnnotationMethodEndpointMapping.class);
		assertEquals(1, endpointMapping.getOrder());
	}

	@Test
	public void annotationActionEndpointMapping() throws Exception {
		AnnotationActionEndpointMapping endpointMapping = this.applicationContext.getBean(
				AnnotationActionEndpointMapping.class);
		assertEquals(2, endpointMapping.getOrder());
	}

	@Test
	public void defaultMethodEndpointAdapter() throws Exception {

		DefaultMethodEndpointAdapter adapter =
				this.applicationContext.getBean(DefaultMethodEndpointAdapter.class);

		assertFalse(adapter.getMethodArgumentResolvers().isEmpty());
		assertFalse(adapter.getMethodReturnValueHandlers().isEmpty());
	}


	@EnableWs
	@Configuration
	public static class TestConfig {

		@Bean(name="testEndpoint")
		public TestEndpoint testEndpoint() {
			return new TestEndpoint();
		}
	}

	@Endpoint
	private static class TestEndpoint {

		@SoapAction("handle")
		public void handle() {
		}

	}

}
