package org.springframework.ws.config.annotation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.endpoint.adapter.method.MethodArgumentResolver;
import org.springframework.ws.server.endpoint.adapter.method.MethodReturnValueHandler;

/**
 * A sub-class of {@code WsConfigurationSupport} that detects and delegates
 * to all beans of type {@link WsConfigurer} allowing them to customize the
 * configuration provided by {@code WsConfigurationSupport}. This is the
 * class actually imported by {@link EnableWs @EnableWs}.
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
	protected void addReturnValueHandlers(
			List<MethodReturnValueHandler> returnValueHandlers) {
		this.configurers.addReturnValueHandlers(returnValueHandlers);
	}
}
