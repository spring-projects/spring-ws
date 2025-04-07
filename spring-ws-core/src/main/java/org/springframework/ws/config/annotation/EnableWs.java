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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

/**
 * Adding this annotation to an {@code @Configuration} class imports the Spring Web
 * Services configuration from {@link WsConfigurationSupport}, for example:
 *
 * <pre><code class='java'>
 * &#064;Configuration
 * &#064;EnableWs
 * &#064;ComponentScan(basePackageClasses = MyConfiguration.class)
 * public class MyConfiguration {
 *
 * }</code></pre>
 * <p>
 * To customize the imported configuration, implement the {@link WsConfigurer} interface
 * or more likely extend the {@link WsConfigurerAdapter} base class and override
 * individual methods, for example:
 *
 * <pre><code class='java'>
 * &#064;Configuration
 * &#064;EnableWs
 * &#064;ComponentScan(basePackageClasses = MyConfiguration.class)
 * public class MyConfiguration extends WsConfigurerAdapter {
 *
 *     &#064;Override
 *     public void addInterceptors(List&lt;EndpointInterceptor&gt; interceptors) {
 *         interceptors.add(new MyInterceptor());
 *     }
 *
 *     &#064;Override
 *     public void addArgumentResolvers(List&lt;MethodArgumentResolver&gt; argumentResolvers) {
 *         argumentResolvers.add(new MyArgumentResolver());
 *     }
 *
 * }</code></pre>
 * <p>
 * <strong>Note:</strong> only one {@code @Configuration} class may have the
 * {@code @EnableWs} annotation to import the Spring Web Services configuration. There can
 * however be multiple {@code @Configuration} classes implementing {@code WsConfigurer} in
 * order to customize the provided configuration.
 * <p>
 * If {@link WsConfigurer} does not expose some more advanced setting that needs to be
 * configured, consider removing the {@code @EnableWs} annotation and extending directly
 * from {@link WsConfigurationSupport} or {@link DelegatingWsConfiguration}, for example:
 *
 * <pre><code class='java'>
 * &#064;Configuration
 * &#064;ComponentScan(basePackageClasses = { MyConfiguration.class })
 * public class MyConfiguration extends WsConfigurationSupport {
 *
 *     &#064;Override
 *     public void addInterceptors(List&lt;EndpointInterceptor&gt; interceptors) {
 *         interceptors.add(new MyInterceptor());
 *     }
 *
 *     &#064;Bean
 *     &#064;Override
 *     public PayloadRootAnnotationMethodEndpointMapping payloadRootAnnotationMethodEndpointMapping() {
 *         // Create or delegate to "super" to create and
 *         // customize properties of PayloadRootAnnotationMethodEndpointMapping
 *     }
 * }</code></pre>
 *
 * @author Arjen Poutsma
 * @author Stephane Nicoll
 * @since 2.2
 * @see WsConfigurer
 * @see WsConfigurerAdapter
 * @see WsConfigurationSupport
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(DelegatingWsConfiguration.class)
public @interface EnableWs {

}
