/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.soap.support;

import org.junit.Assert;
import org.junit.Test;

public class SoapUtilsTest {

	@Test
	public void testExtractActionFromContentType() throws Exception {
		String soapAction = "http://springframework.org/spring-ws/Action";

		String contentType = "application/soap+xml; action=" + soapAction;
		String result = SoapUtils.extractActionFromContentType(contentType);
		Assert.assertEquals("Invalid SOAP action", soapAction, result);

		contentType = "application/soap+xml; action	  = " + soapAction;
		result = SoapUtils.extractActionFromContentType(contentType);
		Assert.assertEquals("Invalid SOAP action", soapAction, result);

		contentType = "application/soap+xml; action=" + soapAction + " ; charset=UTF-8";
		result = SoapUtils.extractActionFromContentType(contentType);
		Assert.assertEquals("Invalid SOAP action", soapAction, result);

		contentType = "application/soap+xml; charset=UTF-8; action=" + soapAction;
		result = SoapUtils.extractActionFromContentType(contentType);
		Assert.assertEquals("Invalid SOAP action", soapAction, result);
	}

	@Test
	public void testEscapeAction() throws Exception {
		String result = SoapUtils.escapeAction("action");
		Assert.assertEquals("Invalid SOAP action", "\"action\"", result);

		result = SoapUtils.escapeAction("\"action\"");
		Assert.assertEquals("Invalid SOAP action", "\"action\"", result);

		result = SoapUtils.escapeAction("");
		Assert.assertEquals("Invalid SOAP action", "\"\"", result);

		result = SoapUtils.escapeAction(null);
		Assert.assertEquals("Invalid SOAP action", "\"\"", result);

	}

	@Test
	public void testSetActionInContentType() throws Exception {
		String soapAction = "http://springframework.org/spring-ws/Action";
		String contentType = "application/soap+xml";

		String result = SoapUtils.setActionInContentType(contentType, soapAction);
		Assert.assertEquals("Invalid SOAP action", soapAction, SoapUtils.extractActionFromContentType(result));

		String anotherSoapAction = "http://springframework.org/spring-ws/AnotherAction";
		String contentTypeWithAction = "application/soap+xml; action=http://springframework.org/spring-ws/Action";
		result = SoapUtils.setActionInContentType(contentTypeWithAction, anotherSoapAction);
		Assert.assertEquals("Invalid SOAP action", anotherSoapAction, SoapUtils.extractActionFromContentType(result));

	}

}