/*
 * Copyright 2005-present the original author or authors.
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

package org.springframework.ws.test.client;

import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.test.support.MockStrategiesHelper;

/**
 * <strong>Main entry point for client-side Web service testing</strong>. Typically used
 * to test a {@link WebServiceTemplate}, set up expectations on request messages, and
 * create response messages.
 * <p>
 * The typical usage of this class is:
 * <ol>
 * <li>Create a {@code MockWebServiceServer} instance by calling
 * {@link #createServer(WebServiceTemplate)},
 * {@link #createServer(WebServiceGatewaySupport)}, or
 * {@link #createServer(ApplicationContext)}.
 * <li>Set up request expectations by calling {@link #expect(RequestMatcher)}, possibly by
 * using the default {@link RequestMatcher} implementations provided in
 * {@link RequestMatchers} (which can be statically imported). Multiple expectations can
 * be set up by chaining {@link ResponseActions#andExpect(RequestMatcher)} calls. To
 * expect a matching request a given number of times, call
 * {@link #expect(ExpectedCount, RequestMatcher)} instead.</li>
 * <li>Create an appropriate response message by calling
 * {@link ResponseActions#andRespond(ResponseCreator) andRespond(ResponseCreator)},
 * possibly by using the default {@link ResponseCreator} implementations provided in
 * {@link ResponseCreators} (which can be statically imported).</li>
 * <li>Use the {@code WebServiceTemplate} as normal, either directly of through client
 * code.</li>
 * <li>Call {@link #verify()}.
 * </ol>
 * Note that because of the 'fluent' API offered by this class (and related classes), you
 * can typically use the Code Completion features (i.e. ctrl-space) in your IDE to set up
 * the mocks.
 * <p>
 * For example: <pre><code class="java">
 * import org.springframework.beans.factory.annotation.Autowired;
 * import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
 * import org.springframework.xml.transform.StringSource;

 * <strong>import org.springframework.ws.test.client.MockWebServiceServer</strong>;
 * <strong>import static org.springframework.ws.test.client.RequestMatchers.*</strong>;
 * <strong>import static org.springframework.ws.test.client.ResponseCreators.*</strong>;
 * import static org.assertj.core.api.Assertions.assertThat;
 *
 * &#064;SpringJUnitConfig(locations = "applicationContext.xml")
 * public class MyWebServiceClientIntegrationTest {
 *
 *	 // MyWebServiceClient extends WebServiceGatewaySupport, and is configured in applicationContext.xml
 *	 &#064;Autowired
 *	 private MyWebServiceClient client;
 *
 *	 private MockWebServiceServer mockServer;
 *
 *	 &#064;Before
 *	 public void createServer() throws Exception {
 *	   <strong>mockServer = MockWebServiceServer.createServer(client)</strong>;
 *	 }
 *
 *	 &#064;Test
 *	 public void getCustomerCount() throws Exception {
 *	   Source expectedRequestPayload =
 *		 new StringSource("&lt;customerCountRequest xmlns=\"http://springframework.org/spring-ws/test\" /&gt;");
 *	   Source responsePayload = new StringSource("&lt;customerCountResponse xmlns='http://springframework.org/spring-ws/test'&gt;" +
 *		 "&lt;customerCount&gt;10&lt;/customerCount&gt;" +
 *		 "&lt;/customerCountResponse&gt;");
 *
 *	   <strong>mockServer.expect(payload(expectedRequestPayload)).andRespond(withPayload(responsePayload));</strong>
 *
 *	   // client.getCustomerCount() uses the WebServiceTemplate
 *	   int customerCount = client.getCustomerCount();
 *	   assertThat(response.getCustomerCount()).isEqualTo(10);
 *
 *	   <strong>mockServer.verify();</strong>
 *	 }
 * }</code></pre>
 *
 * @author Arjen Poutsma
 * @author Lukas Krecan
 * @author Greg Turnquist
 * @author Sharang Gupta
 * @since 2.0
 */
public class MockWebServiceServer {

	private final MockWebServiceMessageSender mockMessageSender;

	public MockWebServiceServer(MockWebServiceMessageSender mockMessageSender) {
		Assert.notNull(mockMessageSender, "'mockMessageSender' must not be null");
		this.mockMessageSender = mockMessageSender;
	}

	/**
	 * Creates a {@code MockWebServiceServer} instance based on the given
	 * {@link WebServiceTemplate}.
	 * @param webServiceTemplate the web service template
	 * @return the created server
	 */
	public static MockWebServiceServer createServer(WebServiceTemplate webServiceTemplate) {
		Assert.notNull(webServiceTemplate, "'webServiceTemplate' must not be null");

		MockWebServiceMessageSender mockMessageSender = new MockWebServiceMessageSender();
		webServiceTemplate.setMessageSender(mockMessageSender);

		return new MockWebServiceServer(mockMessageSender);
	}

	/**
	 * Creates a {@code MockWebServiceServer} instance based on the given
	 * {@link WebServiceGatewaySupport}.
	 * @param gatewaySupport the client class
	 * @return the created server
	 */
	public static MockWebServiceServer createServer(WebServiceGatewaySupport gatewaySupport) {
		Assert.notNull(gatewaySupport, "'gatewaySupport' must not be null");
		return createServer(gatewaySupport.getWebServiceTemplate());
	}

	/**
	 * Creates a {@code MockWebServiceServer} instance based on the given
	 * {@link ApplicationContext}.
	 * <p>
	 * This factory method will try and find a configured {@link WebServiceTemplate} in
	 * the given application context. If no template can be found, it will try and find a
	 * {@link WebServiceGatewaySupport}, and use its configured template. If neither can
	 * be found, an exception is thrown.
	 * @param applicationContext the application context to base the client on
	 * @return the created server
	 * @throws IllegalArgumentException if the given application context contains neither
	 * a {@link WebServiceTemplate} nor a {@link WebServiceGatewaySupport}.
	 */
	public static MockWebServiceServer createServer(ApplicationContext applicationContext) {
		MockStrategiesHelper strategiesHelper = new MockStrategiesHelper(applicationContext);
		WebServiceTemplate webServiceTemplate = strategiesHelper.getStrategy(WebServiceTemplate.class);
		if (webServiceTemplate != null) {
			return createServer(webServiceTemplate);
		}
		WebServiceGatewaySupport gatewaySupport = strategiesHelper.getStrategy(WebServiceGatewaySupport.class);
		if (gatewaySupport != null) {
			return createServer(gatewaySupport);
		}
		throw new IllegalArgumentException(
				"Could not find either WebServiceTemplate or WebServiceGatewaySupport in application context");
	}

	/**
	 * Records an expectation specified by the given {@link RequestMatcher} that is
	 * expected to be met exactly once. Returns a {@link ResponseActions} object that
	 * allows for creating the response, or to set up more expectations.
	 * <p>
	 * This is a shortcut for {@code expect(ExpectedCount.once(), requestMatcher)}.
	 * @param requestMatcher the request matcher expected
	 * @return the response actions
	 * @see #expect(ExpectedCount, RequestMatcher)
	 */
	public ResponseActions expect(RequestMatcher requestMatcher) {
		return expect(ExpectedCount.once(), requestMatcher);
	}

	/**
	 * Records an expectation specified by the given {@link RequestMatcher} that is
	 * expected to be met the given number of times. Returns a {@link ResponseActions}
	 * object that allows for creating the response, or to set up more expectations.
	 * <p>
	 * The expectations are met in the order in which they are recorded: a request is
	 * matched against the expectations that already matched a request and can match more
	 * of them first, and then against the next recorded expectation.
	 * <p>
	 * The response is created for each request that meets the expectation. The response
	 * creators of {@link ResponseCreators} support this, and a custom
	 * {@link ResponseCreator} has to support it as well. The request matchers of an
	 * expectation that can be met more than once are also called with requests that
	 * another expectation meets, so they must not have side effects.
	 * @param count the expected number of matching requests
	 * @param requestMatcher the request matcher expected
	 * @return the response actions
	 * @since 5.1
	 */
	public ResponseActions expect(ExpectedCount count, RequestMatcher requestMatcher) {
		Assert.notNull(count, "'count' must not be null");
		Assert.notNull(requestMatcher, "'requestMatcher' must not be null");
		return this.mockMessageSender.expect(count).andExpect(requestMatcher);
	}

	/**
	 * Verifies that all of the {@link MockWebServiceMessageSender}'s expectations were
	 * met, at least the minimum number of times of their {@link ExpectedCount}.
	 * @throws AssertionError in case of unmet expectations
	 */
	public void verify() {
		this.mockMessageSender.verifyConnections();
	}

	/**
	 * Reset the {@link MockWebServiceMessageSender}'s expectations.
	 */
	public void reset() {
		this.mockMessageSender.reset();
	}

}
