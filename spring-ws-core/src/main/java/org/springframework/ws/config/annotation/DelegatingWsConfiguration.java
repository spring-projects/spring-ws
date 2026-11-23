/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.config.annotation;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.endpoint.adapter.method.MethodArgumentResolver;
import org.springframework.ws.server.endpoint.adapter.method.MethodReturnValueHandler;

/**
 * A subclass of {@code WsConfigurationSupport} that detects and delegates to all beans of
 * type {@link WsConfigurer} allowing them to customize the configuration provided by
 * {@code WsConfigurationSupport}. This is the class actually imported by
 * {@link EnableWs @EnableWs}.
 *
 * @author Arjen Poutsma
 * @since 2.2
 */
@Configuration
public class DelegatingWsConfiguration extends WsConfigurationSupport {

	private WsConfigurers configurers = new WsConfigurers(Collections.emptyList());

	@Autowired
	public void setConfigurers(ObjectProvider<WsConfigurer> configurers) {
		this.configurers = new WsConfigurers(configurers);
	}

	@Override
	protected void addInterceptors(List<EndpointInterceptor> interceptors) {
		this.configurers.addInterceptors(interceptors);
	}

	@Override
	protected void addArgumentResolvers(List<MethodArgumentResolver> argumentResolvers) {
		this.configurers.addArgumentResolvers(argumentResolvers);
	}

	@Override
	protected void addReturnValueHandlers(List<MethodReturnValueHandler> returnValueHandlers) {
		this.configurers.addReturnValueHandlers(returnValueHandlers);
	}

	private static final class WsConfigurers implements WsConfigurer {

		private final Supplier<Stream<WsConfigurer>> delegates;

		WsConfigurers(ObjectProvider<WsConfigurer> wsConfigurers) {
			this.delegates = wsConfigurers::stream;
		}

		WsConfigurers(List<WsConfigurer> wsConfigurers) {
			this.delegates = wsConfigurers::stream;
		}

		@Override
		public void addInterceptors(List<EndpointInterceptor> interceptors) {
			this.delegates.get().forEach(configurer -> configurer.addInterceptors(interceptors));
		}

		@Override
		public void addArgumentResolvers(List<MethodArgumentResolver> argumentResolvers) {
			this.delegates.get().forEach(configurer -> configurer.addArgumentResolvers(argumentResolvers));
		}

		@Override
		public void addReturnValueHandlers(List<MethodReturnValueHandler> returnValueHandlers) {
			this.delegates.get().forEach(configurer -> configurer.addReturnValueHandlers(returnValueHandlers));
		}

	}

}
