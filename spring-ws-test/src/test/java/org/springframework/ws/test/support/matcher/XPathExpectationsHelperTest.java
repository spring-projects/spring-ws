/*
 * Copyright 2005-2011 the original author or authors.
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

package org.springframework.ws.test.support.matcher;

import static org.assertj.core.api.Assertions.*;
import static org.easymock.EasyMock.*;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.ws.WebServiceMessage;
import org.springframework.xml.transform.StringSource;

public class XPathExpectationsHelperTest {

	@Test
	public void existsMatch() throws IOException, AssertionError {

		XPathExpectationsHelper helper = new XPathExpectationsHelper("//b");
		WebServiceMessageMatcher matcher = helper.exists();

		assertThat(matcher).isNotNull();

		WebServiceMessage message = createMock(WebServiceMessage.class);
		expect(message.getPayloadSource()).andReturn(new StringSource("<a><b/></a>"));

		replay(message);

		matcher.match(message);

		verify(message);
	}

	@Test
	public void existsNonMatch() throws AssertionError {

		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {

			XPathExpectationsHelper helper = new XPathExpectationsHelper("//c");
			WebServiceMessageMatcher matcher = helper.exists();

			assertThat(matcher).isNotNull();

			WebServiceMessage message = createMock(WebServiceMessage.class);
			expect(message.getPayloadSource()).andReturn(new StringSource("<a><b/></a>")).times(2);

			replay(message);

			matcher.match(message);
		});
	}

	@Test
	public void doesNotExistMatch() throws IOException, AssertionError {

		XPathExpectationsHelper helper = new XPathExpectationsHelper("//c");
		WebServiceMessageMatcher matcher = helper.doesNotExist();

		assertThat(matcher).isNotNull();

		WebServiceMessage message = createMock(WebServiceMessage.class);
		expect(message.getPayloadSource()).andReturn(new StringSource("<a><b/></a>"));

		replay(message);

		matcher.match(message);

		verify(message);
	}

	@Test
	public void doesNotExistNonMatch() throws AssertionError {

		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {

			XPathExpectationsHelper helper = new XPathExpectationsHelper("//a");
			WebServiceMessageMatcher matcher = helper.doesNotExist();

			assertThat(matcher).isNotNull();

			WebServiceMessage message = createMock(WebServiceMessage.class);
			expect(message.getPayloadSource()).andReturn(new StringSource("<a><b/></a>")).times(2);

			replay(message);

			matcher.match(message);
		});
	}

	@Test
	public void evaluatesToTrueMatch() throws IOException, AssertionError {

		XPathExpectationsHelper helper = new XPathExpectationsHelper("//b=1");
		WebServiceMessageMatcher matcher = helper.evaluatesTo(true);

		assertThat(matcher).isNotNull();

		WebServiceMessage message = createMock(WebServiceMessage.class);
		expect(message.getPayloadSource()).andReturn(new StringSource("<a><b>1</b></a>")).times(2);

		replay(message);

		matcher.match(message);

		verify(message);
	}

	@Test
	public void evaluatesToTrueNonMatch() throws AssertionError {

		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {

			XPathExpectationsHelper helper = new XPathExpectationsHelper("//b=2");
			WebServiceMessageMatcher matcher = helper.evaluatesTo(true);

			assertThat(matcher).isNotNull();

			WebServiceMessage message = createMock(WebServiceMessage.class);
			expect(message.getPayloadSource()).andReturn(new StringSource("<a><b>1</b></a>")).times(2);

			replay(message);

			matcher.match(message);
		});
	}

	@Test
	public void evaluatesToFalseMatch() throws IOException, AssertionError {

		XPathExpectationsHelper helper = new XPathExpectationsHelper("//b!=1");
		WebServiceMessageMatcher matcher = helper.evaluatesTo(false);

		assertThat(matcher).isNotNull();

		WebServiceMessage message = createMock(WebServiceMessage.class);
		expect(message.getPayloadSource()).andReturn(new StringSource("<a><b>1</b></a>")).times(2);

		replay(message);

		matcher.match(message);

		verify(message);
	}

	@Test
	public void evaluatesToFalseNonMatch() throws AssertionError {

		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {

			XPathExpectationsHelper helper = new XPathExpectationsHelper("//b!=2");
			WebServiceMessageMatcher matcher = helper.evaluatesTo(false);

			assertThat(matcher).isNotNull();

			WebServiceMessage message = createMock(WebServiceMessage.class);
			expect(message.getPayloadSource()).andReturn(new StringSource("<a><b>1</b></a>")).times(2);

			replay(message);

			matcher.match(message);
		});
	}

	@Test
	public void evaluatesToIntegerMatch() throws IOException, AssertionError {

		XPathExpectationsHelper helper = new XPathExpectationsHelper("//b");
		WebServiceMessageMatcher matcher = helper.evaluatesTo(1);

		assertThat(matcher).isNotNull();

		WebServiceMessage message = createMock(WebServiceMessage.class);
		expect(message.getPayloadSource()).andReturn(new StringSource("<a><b>1</b></a>")).times(2);

		replay(message);

		matcher.match(message);

		verify(message);
	}

	@Test
	public void evaluatesToIntegerNonMatch() throws AssertionError {

		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {

			XPathExpectationsHelper helper = new XPathExpectationsHelper("//b");
			WebServiceMessageMatcher matcher = helper.evaluatesTo(2);

			assertThat(matcher).isNotNull();

			WebServiceMessage message = createMock(WebServiceMessage.class);
			expect(message.getPayloadSource()).andReturn(new StringSource("<a><b>1</b></a>")).times(2);

			replay(message);

			matcher.match(message);
		});
	}

	@Test
	public void existsWithNamespacesMatch() throws IOException, AssertionError {

		Map<String, String> ns = Collections.singletonMap("x", "http://example.org");
		XPathExpectationsHelper helper = new XPathExpectationsHelper("//x:b", ns);
		WebServiceMessageMatcher matcher = helper.exists();

		assertThat(matcher).isNotNull();

		WebServiceMessage message = createMock(WebServiceMessage.class);
		expect(message.getPayloadSource()).andReturn(new StringSource("<a:a xmlns:a=\"http://example.org\"><a:b/></a:a>"));

		replay(message);

		matcher.match(message);

		verify(message);
	}

	@Test
	public void existsWithNamespacesNonMatch() throws AssertionError {

		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {

			Map<String, String> ns = Collections.singletonMap("x", "http://example.org");
			XPathExpectationsHelper helper = new XPathExpectationsHelper("//b", ns);
			WebServiceMessageMatcher matcher = helper.exists();

			assertThat(matcher).isNotNull();

			WebServiceMessage message = createMock(WebServiceMessage.class);
			expect(message.getPayloadSource()).andReturn(new StringSource("<a:a xmlns:a=\"http://example.org\"><a:b/></a:a>"))
					.times(2);

			replay(message);

			matcher.match(message);
		});
	}
}
