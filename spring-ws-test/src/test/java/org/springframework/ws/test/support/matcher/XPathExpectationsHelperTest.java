/*
 * Copyright 2005-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.test.support.matcher;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.springframework.ws.WebServiceMessage;
import org.springframework.xml.transform.StringSource;

import org.junit.Test;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertNotNull;

public class XPathExpectationsHelperTest {

	@Test
	public void existsMatch() throws IOException, AssertionError {
		XPathExpectationsHelper helper = new XPathExpectationsHelper("//b");
		WebServiceMessageMatcher matcher = helper.exists();
		assertNotNull(matcher);

		WebServiceMessage message = createMock(WebServiceMessage.class);
		expect(message.getPayloadSource()).andReturn(new StringSource("<a><b/></a>"));

		replay(message);

		matcher.match(message);

		verify(message);
	}

	@Test(expected = AssertionError.class)
	public void existsNonMatch() throws IOException, AssertionError {
		XPathExpectationsHelper helper = new XPathExpectationsHelper("//c");
		WebServiceMessageMatcher matcher = helper.exists();
		assertNotNull(matcher);

		WebServiceMessage message = createMock(WebServiceMessage.class);
		expect(message.getPayloadSource()).andReturn(new StringSource("<a><b/></a>")).times(2);

		replay(message);

		matcher.match(message);
	}

	@Test
	public void doesNotExistMatch() throws IOException, AssertionError {
		XPathExpectationsHelper helper = new XPathExpectationsHelper("//c");
		WebServiceMessageMatcher matcher = helper.doesNotExist();
		assertNotNull(matcher);

		WebServiceMessage message = createMock(WebServiceMessage.class);
		expect(message.getPayloadSource()).andReturn(new StringSource("<a><b/></a>"));

		replay(message);

		matcher.match(message);

		verify(message);
	}

	@Test(expected = AssertionError.class)
	public void doesNotExistNonMatch() throws IOException, AssertionError {
		XPathExpectationsHelper helper = new XPathExpectationsHelper("//a");
		WebServiceMessageMatcher matcher = helper.doesNotExist();
		assertNotNull(matcher);

		WebServiceMessage message = createMock(WebServiceMessage.class);
		expect(message.getPayloadSource()).andReturn(new StringSource("<a><b/></a>")).times(2);

		replay(message);

		matcher.match(message);
	}

	@Test
	public void evaluatesToTrueMatch() throws IOException, AssertionError {
		XPathExpectationsHelper helper = new XPathExpectationsHelper("//b=1");
		WebServiceMessageMatcher matcher = helper.evaluatesTo(true);
		assertNotNull(matcher);

		WebServiceMessage message = createMock(WebServiceMessage.class);
		expect(message.getPayloadSource()).andReturn(new StringSource("<a><b>1</b></a>")).times(2);

		replay(message);

		matcher.match(message);

		verify(message);
	}

	@Test(expected = AssertionError.class)
	public void evaluatesToTrueNonMatch() throws IOException, AssertionError {
		XPathExpectationsHelper helper = new XPathExpectationsHelper("//b=2");
		WebServiceMessageMatcher matcher = helper.evaluatesTo(true);
		assertNotNull(matcher);

		WebServiceMessage message = createMock(WebServiceMessage.class);
		expect(message.getPayloadSource()).andReturn(new StringSource("<a><b>1</b></a>")).times(2);

		replay(message);

		matcher.match(message);
	}

	@Test
	public void evaluatesToFalseMatch() throws IOException, AssertionError {
		XPathExpectationsHelper helper = new XPathExpectationsHelper("//b!=1");
		WebServiceMessageMatcher matcher = helper.evaluatesTo(false);
		assertNotNull(matcher);

		WebServiceMessage message = createMock(WebServiceMessage.class);
		expect(message.getPayloadSource()).andReturn(new StringSource("<a><b>1</b></a>")).times(2);

		replay(message);

		matcher.match(message);

		verify(message);
	}

	@Test(expected = AssertionError.class)
	public void evaluatesToFalseNonMatch() throws IOException, AssertionError {
		XPathExpectationsHelper helper = new XPathExpectationsHelper("//b!=2");
		WebServiceMessageMatcher matcher = helper.evaluatesTo(false);
		assertNotNull(matcher);

		WebServiceMessage message = createMock(WebServiceMessage.class);
		expect(message.getPayloadSource()).andReturn(new StringSource("<a><b>1</b></a>")).times(2);

		replay(message);

		matcher.match(message);
	}

	@Test
	public void evaluatesToIntegerMatch() throws IOException, AssertionError {
		XPathExpectationsHelper helper = new XPathExpectationsHelper("//b");
		WebServiceMessageMatcher matcher = helper.evaluatesTo(1);
		assertNotNull(matcher);

		WebServiceMessage message = createMock(WebServiceMessage.class);
		expect(message.getPayloadSource()).andReturn(new StringSource("<a><b>1</b></a>")).times(2);

		replay(message);

		matcher.match(message);

		verify(message);
	}

	@Test(expected = AssertionError.class)
	public void evaluatesToIntegerNonMatch() throws IOException, AssertionError {
		XPathExpectationsHelper helper = new XPathExpectationsHelper("//b");
		WebServiceMessageMatcher matcher = helper.evaluatesTo(2);
		assertNotNull(matcher);

		WebServiceMessage message = createMock(WebServiceMessage.class);
		expect(message.getPayloadSource()).andReturn(new StringSource("<a><b>1</b></a>")).times(2);

		replay(message);

		matcher.match(message);
	}

	@Test
	public void existsWithNamespacesMatch() throws IOException, AssertionError {
		Map<String, String> ns = Collections.singletonMap("x", "http://example.org");
		XPathExpectationsHelper helper = new XPathExpectationsHelper("//x:b", ns);
		WebServiceMessageMatcher matcher = helper.exists();
		assertNotNull(matcher);

		WebServiceMessage message = createMock(WebServiceMessage.class);
		expect(message.getPayloadSource())
				.andReturn(new StringSource("<a:a xmlns:a=\"http://example.org\"><a:b/></a:a>"));

		replay(message);

		matcher.match(message);

		verify(message);
	}

	@Test(expected = AssertionError.class)
	public void existsWithNamespacesNonMatch() throws IOException, AssertionError {
		Map<String, String> ns = Collections.singletonMap("x", "http://example.org");
		XPathExpectationsHelper helper = new XPathExpectationsHelper("//b", ns);
		WebServiceMessageMatcher matcher = helper.exists();
		assertNotNull(matcher);

		WebServiceMessage message = createMock(WebServiceMessage.class);
		expect(message.getPayloadSource())
				.andReturn(new StringSource("<a:a xmlns:a=\"http://example.org\"><a:b/></a:a>")).times(2);

		replay(message);

		matcher.match(message);
	}

}
