package org.springframework.ws.config.annotation;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.endpoint.adapter.DefaultMethodEndpointAdapter;
import org.springframework.ws.server.endpoint.adapter.method.MethodArgumentResolver;
import org.springframework.ws.server.endpoint.adapter.method.MethodReturnValueHandler;
import org.springframework.ws.server.endpoint.interceptor.EndpointInterceptorAdapter;
import org.springframework.ws.server.endpoint.mapping.PayloadRootAnnotationMethodEndpointMapping;

/**
 * @author Arjen Poutsma
 */
public class WsConfigurerAdapterTest {

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
	public void argumentResolvers() {
		DefaultMethodEndpointAdapter endpointAdapter = this.applicationContext.getBean(DefaultMethodEndpointAdapter.class);

		List<MethodArgumentResolver> argumentResolvers = endpointAdapter.getCustomMethodArgumentResolvers();
		assertEquals(1, argumentResolvers.size());
		assertTrue(argumentResolvers.get(0) instanceof MyMethodArgumentResolver);

		argumentResolvers = endpointAdapter.getMethodArgumentResolvers();
		assertFalse(argumentResolvers.isEmpty());
	}

	@Test
	public void returnValueHandlers() {
		DefaultMethodEndpointAdapter endpointAdapter = this.applicationContext.getBean(DefaultMethodEndpointAdapter.class);

		List<MethodReturnValueHandler> returnValueHandlers = endpointAdapter.getCustomMethodReturnValueHandlers();
		assertEquals(1, returnValueHandlers.size());
		assertTrue(returnValueHandlers.get(0) instanceof MyReturnValueHandler);

		returnValueHandlers = endpointAdapter.getMethodReturnValueHandlers();
		assertFalse(returnValueHandlers.isEmpty());
	}

	@Configuration
	@EnableWs
	public static class TestConfig extends WsConfigurerAdapter {

		@Override
		public void addInterceptors(List<EndpointInterceptor> interceptors) {
			interceptors.add(new MyInterceptor());
		}

		@Override
		public void addArgumentResolvers(List<MethodArgumentResolver> argumentResolvers) {
			argumentResolvers.add(new MyMethodArgumentResolver());
		}

		@Override
		public void addReturnValueHandlers(List<MethodReturnValueHandler> returnValueHandlers) {
			returnValueHandlers.add(new MyReturnValueHandler());
		}
	}

	public static class MyInterceptor extends EndpointInterceptorAdapter {}

	public static class MyMethodArgumentResolver implements MethodArgumentResolver {

		@Override
		public boolean supportsParameter(MethodParameter parameter) {
			return false;
		}

		@Override
		public Object resolveArgument(MessageContext messageContext, MethodParameter parameter) throws Exception {
			return null;
		}
	}

	public static class MyReturnValueHandler implements MethodReturnValueHandler {

		@Override
		public boolean supportsReturnType(MethodParameter returnType) {
			return false;
		}

		@Override
		public void handleReturnValue(MessageContext messageContext, MethodParameter returnType, Object returnValue)
				throws Exception {}
	}

}
