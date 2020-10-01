package org.springframework.ws.config.annotation;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

	@BeforeEach
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

		assertThat(endpointMapping.getOrder()).isEqualTo(0);

		EndpointInterceptor[] interceptors = endpointMapping.getInterceptors();

		assertThat(interceptors).hasSize(1);
		assertThat(interceptors[0]).isInstanceOf(MyInterceptor.class);
	}

	@Test
	public void argumentResolvers() {

		DefaultMethodEndpointAdapter endpointAdapter = this.applicationContext.getBean(DefaultMethodEndpointAdapter.class);

		List<MethodArgumentResolver> argumentResolvers = endpointAdapter.getCustomMethodArgumentResolvers();

		assertThat(argumentResolvers).hasSize(1);
		assertThat(argumentResolvers.get(0)).isInstanceOf(MyMethodArgumentResolver.class);

		argumentResolvers = endpointAdapter.getMethodArgumentResolvers();

		assertThat(argumentResolvers).isNotEmpty();
	}

	@Test
	public void returnValueHandlers() {

		DefaultMethodEndpointAdapter endpointAdapter = this.applicationContext.getBean(DefaultMethodEndpointAdapter.class);

		List<MethodReturnValueHandler> returnValueHandlers = endpointAdapter.getCustomMethodReturnValueHandlers();

		assertThat(returnValueHandlers).hasSize(1);
		assertThat(returnValueHandlers.get(0)).isInstanceOf(MyReturnValueHandler.class);

		returnValueHandlers = endpointAdapter.getMethodReturnValueHandlers();

		assertThat(returnValueHandlers).isNotEmpty();
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
