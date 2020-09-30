package org.springframework.ws.config.annotation;

import java.util.List;

import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.endpoint.adapter.method.MethodArgumentResolver;
import org.springframework.ws.server.endpoint.adapter.method.MethodReturnValueHandler;

/**
 * Defines callback methods to customize the Java-based configuration for Spring Web Services enabled via
 * {@link EnableWs @EnableWs}.
 * <p>
 * {@code @EnableWs}-annotated configuration classes may implement this interface to be called back and given a chance
 * to customize the default configuration. Consider extending {@link WsConfigurerAdapter}, which provides a stub
 * implementation of all interface methods.
 *
 * @author Arjen Poutsma
 * @since 2.2
 */
public interface WsConfigurer {

	/**
	 * Add {@link EndpointInterceptor}s for pre- and post-processing of endpoint method invocations.
	 */
	void addInterceptors(List<EndpointInterceptor> interceptors);

	/**
	 * Add resolvers to support custom endpoint method argument types.
	 * 
	 * @param argumentResolvers initially an empty list
	 */
	void addArgumentResolvers(List<MethodArgumentResolver> argumentResolvers);

	/**
	 * Add handlers to support custom controller method return value types.
	 * <p>
	 * Using this option does not override the built-in support for handling return values. To customize the built-in
	 * support for handling return values, configure RequestMappingHandlerAdapter directly.
	 * 
	 * @param returnValueHandlers initially an empty list
	 */
	void addReturnValueHandlers(List<MethodReturnValueHandler> returnValueHandlers);
}
