/*
 * Copyright 2005-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.soap.support;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class SoapUtilsTest {

	@Test
	public void testExtractActionFromContentType() {

		String soapAction = "http://springframework.org/spring-ws/Action";

		String contentType = "application/soap+xml; action=" + soapAction;
		String result = SoapUtils.extractActionFromContentType(contentType);

		assertThat(result).isEqualTo(soapAction);

		contentType = "application/soap+xml; action	  = " + soapAction;
		result = SoapUtils.extractActionFromContentType(contentType);

		assertThat(result).isEqualTo(soapAction);

		contentType = "application/soap+xml; action=" + soapAction + " ; charset=UTF-8";
		result = SoapUtils.extractActionFromContentType(contentType);

		assertThat(result).isEqualTo(soapAction);

		contentType = "application/soap+xml; charset=UTF-8; action=" + soapAction;
		result = SoapUtils.extractActionFromContentType(contentType);

		assertThat(result).isEqualTo(soapAction);
	}

	@Test
	public void testEscapeAction() {

		String result = SoapUtils.escapeAction("action");

		assertThat(result).isEqualTo("\"action\"");

		result = SoapUtils.escapeAction("\"action\"");

		assertThat(result).isEqualTo("\"action\"");

		result = SoapUtils.escapeAction("");

		assertThat(result).isEqualTo("\"\"");

		result = SoapUtils.escapeAction(null);

		assertThat(result).isEqualTo("\"\"");

	}

	@Test
	public void testSetActionInContentType() {

		String soapAction = "http://springframework.org/spring-ws/Action";
		String contentType = "application/soap+xml";

		String result = SoapUtils.setActionInContentType(contentType, soapAction);

		assertThat(SoapUtils.extractActionFromContentType(result)).isEqualTo(soapAction);

		String anotherSoapAction = "http://springframework.org/spring-ws/AnotherAction";
		String contentTypeWithAction = "application/soap+xml; action=http://springframework.org/spring-ws/Action";
		result = SoapUtils.setActionInContentType(contentTypeWithAction, anotherSoapAction);

		assertThat(SoapUtils.extractActionFromContentType(result)).isEqualTo(anotherSoapAction);
	}
}
