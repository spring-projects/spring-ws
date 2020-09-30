package org.springframework.ws.config.annotation;

import java.util.List;

import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.endpoint.adapter.method.MethodArgumentResolver;
import org.springframework.ws.server.endpoint.adapter.method.MethodReturnValueHandler;

/**
 * An default implementation of {@link WsConfigurer} with empty methods allowing sub-classes to override only the
 * methods they're interested in.
 *
 * @author Arjen Poutsma
 * @since 2.2
 */
public class WsConfigurerAdapter implements WsConfigurer {

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation is empty.
	 */
	@Override
	public void addInterceptors(List<EndpointInterceptor> interceptors) {}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation is empty.
	 */
	@Override
	public void addArgumentResolvers(List<MethodArgumentResolver> argumentResolvers) {}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation is empty.
	 */
	@Override
	public void addReturnValueHandlers(List<MethodReturnValueHandler> returnValueHandlers) {

	}
}
