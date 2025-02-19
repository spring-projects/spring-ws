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

import java.util.ArrayList;
import java.util.List;

import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.endpoint.adapter.method.MethodArgumentResolver;
import org.springframework.ws.server.endpoint.adapter.method.MethodReturnValueHandler;

/**
 * An {@link WsConfigurer} implementation that delegates to other {@link WsConfigurer}
 * instances.
 *
 * @author Arjen Poutsma
 * @since 2.2
 */
public class WsConfigurerComposite implements WsConfigurer {

	private List<WsConfigurer> delegates = new ArrayList<WsConfigurer>();

	public void addWsConfigurers(List<WsConfigurer> configurers) {
		if (configurers != null) {
			this.delegates.addAll(configurers);
		}
	}

	@Override
	public void addInterceptors(List<EndpointInterceptor> interceptors) {
		for (WsConfigurer delegate : delegates) {
			delegate.addInterceptors(interceptors);
		}
	}

	@Override
	public void addArgumentResolvers(List<MethodArgumentResolver> argumentResolvers) {
		for (WsConfigurer delegate : delegates) {
			delegate.addArgumentResolvers(argumentResolvers);
		}
	}

	@Override
	public void addReturnValueHandlers(List<MethodReturnValueHandler> returnValueHandlers) {
		for (WsConfigurer delegate : delegates) {
			delegate.addReturnValueHandlers(returnValueHandlers);
		}
	}

}
