/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.config.annotation;

import java.util.List;

import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.endpoint.adapter.method.MethodArgumentResolver;
import org.springframework.ws.server.endpoint.adapter.method.MethodReturnValueHandler;

/**
 * Defines callback methods to customize the Java-based configuration for Spring Web
 * Services enabled via {@link EnableWs @EnableWs}.
 * <p>
 * {@code @EnableWs}-annotated configuration classes may implement this interface to be
 * called back and given a chance to customize the default configuration.
 *
 * @author Arjen Poutsma
 * @since 2.2
 */
public interface WsConfigurer {

	/**
	 * Add {@link EndpointInterceptor}s for pre- and post-processing of endpoint method
	 * invocations.
	 */
	default void addInterceptors(List<EndpointInterceptor> interceptors) {

	}

	/**
	 * Configure the {@link MethodArgumentResolver}s to use in addition to the ones
	 * registered by default.
	 * @param argumentResolvers the list of resolvers; initially the default resolvers
	 */
	default void addArgumentResolvers(List<MethodArgumentResolver> argumentResolvers) {

	}

	/**
	 * Configure the {@link MethodReturnValueHandler}s to use in addition to the ones
	 * registered by default.
	 * @param returnValueHandlers the list of handlers; initially the default handlers
	 */
	default void addReturnValueHandlers(List<MethodReturnValueHandler> returnValueHandlers) {

	}

}
