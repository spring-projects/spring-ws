/*
 * Copyright 2005-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
	 * Add resolvers to support custom endpoint method argument types.
	 * @param argumentResolvers initially an empty list
	 */
	default void addArgumentResolvers(List<MethodArgumentResolver> argumentResolvers) {

	}

	/**
	 * Add handlers to support custom controller method return value types.
	 * <p>
	 * Using this option does not override the built-in support for handling return
	 * values. To customize the built-in support for handling return values, configure
	 * RequestMappingHandlerAdapter directly.
	 * @param returnValueHandlers initially an empty list
	 */
	default void addReturnValueHandlers(List<MethodReturnValueHandler> returnValueHandlers) {

	}

}
