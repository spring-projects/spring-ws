/*
 * Copyright 2005-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.config.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Add this annotation to an {@link Configuration @Configuration} class to have the Spring Web Services configuration
 * defined in {@link WsConfigurationSupport} imported. For instance:
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableWs
 * &#064;ComponentScan(basePackageClasses = { MyConfiguration.class })
 * public class MyWsConfiguration {
 *
 * }
 * </pre>
 * <p>
 * Customize the imported configuration by implementing the {@link WsConfigurer} interface or more likely by extending
 * the {@link WsConfigurerAdapter} base class and overriding individual methods:
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableWs
 * &#064;ComponentScan(basePackageClasses = { MyConfiguration.class })
 * public class MyConfiguration extends WsConfigurerAdapter {
 *
 * 	&#064;Override
 * 	public void addInterceptors(List&lt;EndpointInterceptor&gt; interceptors) {
 * 		interceptors.add(new MyInterceptor());
 * 	}
 *
 * 	&#064;Override
 * 	public void addArgumentResolvers(List&lt;MethodArgumentResolver&gt; argumentResolvers) {
 * 		argumentResolvers.add(new MyArgumentResolver());
 * 	}
 *
 * 	// More overridden methods ...
 * }
 * </pre>
 * <p>
 * If the customization options of {@link WsConfigurer} do not expose something you need to configure, consider removing
 * the {@code @EnableWs} annotation and extending directly from {@link WsConfigurationSupport} overriding selected
 * {@code @Bean} methods:
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;ComponentScan(basePackageClasses = { MyConfiguration.class })
 * public class MyConfiguration extends WsConfigurationSupport {
 *
 * 	&#064;Override
 * 	public void addInterceptors(List&lt;EndpointInterceptor&gt; interceptors) {
 * 		interceptors.add(new MyInterceptor());
 * 	}
 *
 * 	&#064;Bean
 * 	&#064;Override
 * 	public DefaultMethodEndpointAdapter defaultMethodEndpointAdapter() {
 * 		// Create or delegate to "super" to create and
 * 		// customize properties of DefaultMethodEndpointAdapter
 * 	}
 * }
 * </pre>
 *
 * @see WsConfigurer
 * @see WsConfigurerAdapter
 * @see WsConfigurationSupport
 * @author Arjen Poutsma
 * @since 2.2
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(DelegatingWsConfiguration.class)
public @interface EnableWs {

}
