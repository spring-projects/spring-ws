package org.springframework.ws.config.annotation;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.endpoint.adapter.DefaultMethodEndpointAdapter;
import org.springframework.ws.server.endpoint.interceptor.EndpointInterceptorAdapter;
import org.springframework.ws.server.endpoint.mapping.PayloadRootAnnotationMethodEndpointMapping;

/**
 * @author Arjen Poutsma
 */
public class WsConfigurationSupportTest {

	private ApplicationContext applicationContext;

	@Before
	public void setUp() throws Exception {
		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
		applicationContext.register(TestConfig.class);
		applicationContext.refresh();

		this.applicationContext = applicationContext;
	}

	@Test
	public void interceptors() {
		PayloadRootAnnotationMethodEndpointMapping endpointMapping = this.applicationContext
				.getBean(PayloadRootAnnotationMethodEndpointMapping.class);
		assertEquals(0, endpointMapping.getOrder());

		EndpointInterceptor[] interceptors = endpointMapping.getInterceptors();
		assertEquals(1, interceptors.length);
		assertTrue(interceptors[0] instanceof MyInterceptor);
	}

	@Test
	public void defaultMethodEndpointAdapter() {
		DefaultMethodEndpointAdapter endpointAdapter = this.applicationContext.getBean(DefaultMethodEndpointAdapter.class);
		assertNotNull(endpointAdapter);

		assertTrue(endpointAdapter instanceof MyDefaultMethodEndpointAdapter);
	}

	@Configuration
	public static class TestConfig extends WsConfigurationSupport {

		@Override
		protected void addInterceptors(List<EndpointInterceptor> interceptors) {
			interceptors.add(new MyInterceptor());
		}

		@Bean
		@Override
		public DefaultMethodEndpointAdapter defaultMethodEndpointAdapter() {
			return new MyDefaultMethodEndpointAdapter();
		}
	}

	public static class MyInterceptor extends EndpointInterceptorAdapter {

	}

	public static class MyDefaultMethodEndpointAdapter extends DefaultMethodEndpointAdapter {

	}

}
