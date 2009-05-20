/*
 * Copyright 2007 the original author or authors.
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

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.mime.MimeMarshaller;
import org.springframework.oxm.mime.MimeUnmarshaller;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.mime.MimeMessage;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

public class MarshallingUtilsTest extends TestCase {

    public void testUnmarshal() throws Exception {
        MockControl unmarshallerControl = MockControl.createControl(Unmarshaller.class);
        Unmarshaller unmarshallerMock = (Unmarshaller) unmarshallerControl.getMock();
        MockControl messageControl = MockControl.createControl(WebServiceMessage.class);
        WebServiceMessage messageMock = (WebServiceMessage) messageControl.getMock();

        Source source = new StringSource("");
        Object unmarshalled = new Object();
        messageControl.expectAndReturn(messageMock.getPayloadSource(), source);
        unmarshallerControl.expectAndReturn(unmarshallerMock.unmarshal(source), unmarshalled);

        unmarshallerControl.replay();
        messageControl.replay();

        Object result = MarshallingUtils.unmarshal(unmarshallerMock, messageMock);
        assertEquals("Invalid unmarshalled object", unmarshalled, result);

        unmarshallerControl.verify();
        messageControl.verify();
    }

    public void testUnmarshalMime() throws Exception {
        MockControl unmarshallerControl = MockControl.createControl(MimeUnmarshaller.class);
        MimeUnmarshaller unmarshallerMock = (MimeUnmarshaller) unmarshallerControl.getMock();
        MockControl messageControl = MockControl.createControl(MimeMessage.class);
        MimeMessage messageMock = (MimeMessage) messageControl.getMock();

        Source source = new StringSource("");
        Object unmarshalled = new Object();
        messageControl.expectAndReturn(messageMock.getPayloadSource(), source);
        unmarshallerMock.unmarshal(source, null);
        unmarshallerControl.setMatcher(MockControl.ALWAYS_MATCHER);
        unmarshallerControl.setReturnValue(unmarshalled);

        unmarshallerControl.replay();
        messageControl.replay();

        Object result = MarshallingUtils.unmarshal(unmarshallerMock, messageMock);
        assertEquals("Invalid unmarshalled object", unmarshalled, result);

        unmarshallerControl.verify();
        messageControl.verify();
    }

    public void testUnmarshalNoPayload() throws Exception {
        MockControl unmarshallerControl = MockControl.createControl(MimeUnmarshaller.class);
        MimeUnmarshaller unmarshallerMock = (MimeUnmarshaller) unmarshallerControl.getMock();
        MockControl messageControl = MockControl.createControl(MimeMessage.class);
        MimeMessage messageMock = (MimeMessage) messageControl.getMock();

        messageControl.expectAndReturn(messageMock.getPayloadSource(), null);

        unmarshallerControl.replay();
        messageControl.replay();

        Object result = MarshallingUtils.unmarshal(unmarshallerMock, messageMock);
        assertNull("Invalid unmarshalled object", result);

        unmarshallerControl.verify();
        messageControl.verify();
    }

    public void testMarshal() throws Exception {
        MockControl marshallerControl = MockControl.createControl(Marshaller.class);
        Marshaller marshallerMock = (Marshaller) marshallerControl.getMock();
        MockControl messageControl = MockControl.createControl(WebServiceMessage.class);
        WebServiceMessage messageMock = (WebServiceMessage) messageControl.getMock();

        Result result = new StringResult();
        Object marshalled = new Object();
        messageControl.expectAndReturn(messageMock.getPayloadResult(), result);
        marshallerMock.marshal(marshalled, result);

        marshallerControl.replay();
        messageControl.replay();

        MarshallingUtils.marshal(marshallerMock, marshalled, messageMock);

        marshallerControl.verify();
        messageControl.verify();
    }

    public void testMarshalMime() throws Exception {
        MockControl marshallerControl = MockControl.createControl(MimeMarshaller.class);
        MimeMarshaller marshallerMock = (MimeMarshaller) marshallerControl.getMock();
        MockControl messageControl = MockControl.createControl(MimeMessage.class);
        MimeMessage messageMock = (MimeMessage) messageControl.getMock();

        Result result = new StringResult();
        Object marshalled = new Object();
        messageControl.expectAndReturn(messageMock.getPayloadResult(), result);
        marshallerMock.marshal(marshalled, result, null);
        marshallerControl.setMatcher(MockControl.ALWAYS_MATCHER);

        marshallerControl.replay();
        messageControl.replay();

        MarshallingUtils.marshal(marshallerMock, marshalled, messageMock);

        marshallerControl.verify();
        messageControl.verify();
    }


}