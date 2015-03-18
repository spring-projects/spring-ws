/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.support;

import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.mime.MimeContainer;
import org.springframework.oxm.mime.MimeMarshaller;
import org.springframework.oxm.mime.MimeUnmarshaller;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.mime.MimeMessage;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

import org.junit.Assert;
import org.junit.Test;

import static org.easymock.EasyMock.*;

public class MarshallingUtilsTest {

	@Test
	public void testUnmarshal() throws Exception {
		Unmarshaller unmarshallerMock = createMock(Unmarshaller.class);
		WebServiceMessage messageMock = createMock(WebServiceMessage.class);

		Source source = new StringSource("");
		Object unmarshalled = new Object();
		expect(messageMock.getPayloadSource()).andReturn(source);
		expect(unmarshallerMock.unmarshal(source)).andReturn(unmarshalled);

		replay(unmarshallerMock, messageMock);

		Object result = MarshallingUtils.unmarshal(unmarshallerMock, messageMock);
		Assert.assertEquals("Invalid unmarshalled object", unmarshalled, result);

		verify(unmarshallerMock, messageMock);
	}

	@Test
	public void testUnmarshalMime() throws Exception {
		MimeUnmarshaller unmarshallerMock = createMock(MimeUnmarshaller.class);
		MimeMessage messageMock = createMock(MimeMessage.class);

		Source source = new StringSource("");
		Object unmarshalled = new Object();
		expect(messageMock.getPayloadSource()).andReturn(source);
		expect(unmarshallerMock.unmarshal(eq(source), isA(MimeContainer.class))).andReturn(unmarshalled);

		replay(unmarshallerMock, messageMock);

		Object result = MarshallingUtils.unmarshal(unmarshallerMock, messageMock);
		Assert.assertEquals("Invalid unmarshalled object", unmarshalled, result);

		verify(unmarshallerMock, messageMock);
	}

	@Test
	public void testUnmarshalNoPayload() throws Exception {
		Unmarshaller unmarshallerMock = createMock(Unmarshaller.class);
		MimeMessage messageMock = createMock(MimeMessage.class);

		expect(messageMock.getPayloadSource()).andReturn(null);

		replay(unmarshallerMock, messageMock);

		Object result = MarshallingUtils.unmarshal(unmarshallerMock, messageMock);
		Assert.assertNull("Invalid unmarshalled object", result);

		verify(unmarshallerMock, messageMock);
	}

	@Test
	public void testMarshal() throws Exception {
		Marshaller marshallerMock = createMock(Marshaller.class);
		WebServiceMessage messageMock = createMock(WebServiceMessage.class);

		Result result = new StringResult();
		Object marshalled = new Object();
		expect(messageMock.getPayloadResult()).andReturn(result);
		marshallerMock.marshal(marshalled, result);

		replay(marshallerMock, messageMock);

		MarshallingUtils.marshal(marshallerMock, marshalled, messageMock);

		verify(marshallerMock, messageMock);
	}

	@Test
	public void testMarshalMime() throws Exception {
		MimeMarshaller marshallerMock = createMock(MimeMarshaller.class);
		MimeMessage messageMock = createMock(MimeMessage.class);

		Result result = new StringResult();
		Object marshalled = new Object();
		expect(messageMock.getPayloadResult()).andReturn(result);
		marshallerMock.marshal(eq(marshalled), eq(result), isA(MimeContainer.class));

		replay(marshallerMock, messageMock);

		MarshallingUtils.marshal(marshallerMock, marshalled, messageMock);

		verify(marshallerMock, messageMock);
	}


}