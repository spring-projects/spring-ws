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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.endpoint.adapter.method.MethodArgumentResolver;
import org.springframework.ws.server.endpoint.adapter.method.MethodReturnValueHandler;

/**
 * A sub-class of {@code WsConfigurationSupport} that detects and delegates to all beans
 * of type {@link WsConfigurer} allowing them to customize the configuration provided by
 * {@code WsConfigurationSupport}. This is the class actually imported by
 * {@link EnableWs @EnableWs}.
 *
 * @author Arjen Poutsma
 * @since 2.2
 */
@Configuration
public class DelegatingWsConfiguration extends WsConfigurationSupport {

	private final WsConfigurerComposite configurers = new WsConfigurerComposite();

	@Autowired(required = false)
	public void setConfigurers(List<WsConfigurer> configurers) {
		if (configurers != null && !configurers.isEmpty()) {
			this.configurers.addWsConfigurers(configurers);
		}
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

}
