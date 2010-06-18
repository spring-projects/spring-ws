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

package org.springframework.ws.mock.client;

import java.io.IOException;

import org.springframework.ws.client.WebServiceIOException;
import org.springframework.ws.client.WebServiceTransportException;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.client.SoapFaultClientException;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

import org.junit.Before;
import org.junit.Test;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;

public class MockWebServiceMessageSenderTest {

    private MockWebServiceMessageSender messageSender;

    private WebServiceTemplate template;

    @Before
    public void setUp() throws Exception {
        messageSender = new MockWebServiceMessageSender();
        template = new WebServiceTemplate();
        template.setMessageSender(messageSender);
        template.setDefaultUri("http://example.com/airline");
    }

    @Test
    public void normal() throws Exception {
        String request = "<request xmlns='http://example.com'/>";
        String response = "<response xmlns='http://example.com'/>";

        messageSender.whenConnecting().expectPayload(request).andRespondWithPayload(response);
        messageSender.replay();

        StringResult result = new StringResult();
        template.sendSourceAndReceiveToResult(new StringSource(request), result);
        assertXMLEqual(result.toString(), response);
        messageSender.verify();
    }

    @Test(expected = AssertionError.class)
    public void invalidRequest() throws Exception {
        String expectedRequest = "<request1 xmlns='http://example.com'/>";

        messageSender.whenConnecting().expectPayload(expectedRequest);
        messageSender.replay();

        String realRequest = "<request2 xmlns='http://example.com'/>";
        template.sendSourceAndReceiveToResult(new StringSource(realRequest), new StringResult());
        messageSender.verify();
    }

    @Test(expected = AssertionError.class)
    public void wrongUri() throws Exception {
        String expectedUri = "http://example.com/1";

        messageSender.whenConnectingTo(expectedUri);
        messageSender.replay();

        String realUri = "http://example.com/2";
        template.sendSourceAndReceiveToResult(realUri, null, null);
        messageSender.verify();
    }
    
    @Test(expected = AssertionError.class)
    public void notAllUris() throws Exception {
        String expectedUri1 = "http://example.com/1";
        String expectedUri2 = "http://example.com/2";
        String request = "<request xmlns='http://example.com'/>";
        String response = "<response xmlns='http://example.com'/>";


        messageSender.whenConnectingTo(expectedUri1).expectPayload(request).andRespondWithPayload(response);
        messageSender.whenConnectingTo(expectedUri2).expectPayload(request).andRespondWithPayload(response);
        messageSender.replay();

        template.sendSourceAndReceiveToResult(expectedUri1, new StringSource(request), new StringResult());
        messageSender.verify();
    }

    @Test(expected = WebServiceTransportException.class)
    public void error() throws Exception {
        String request = "<root xmlns='http://example.com'/>";

        messageSender.whenConnecting().expectPayload(request).andRespondWithError("Something went wrong");
        messageSender.replay();
        
        template.sendSourceAndReceiveToResult(new StringSource(request), new StringResult());
        messageSender.verify();
    }

    @Test(expected = WebServiceIOException.class)
    public void ioException() throws Exception {
        String request = "<root xmlns='http://example.com'/>";

        IOException ex = new IOException("Something went wrong");
        messageSender.whenConnecting().expectPayload(request).andThrowException(ex);
        messageSender.replay();

        template.sendSourceAndReceiveToResult(new StringSource(request), new StringResult());
        messageSender.verify();
    }

    @Test(expected = SoapFaultClientException.class)
    public void soapFault() throws Exception {
        String request = "<root xmlns='http://example.com'/>";

        String faultString = "Foo";
        messageSender.whenConnecting().expectPayload(request).andRespondWithMustUnderstandFault(faultString, null);
        messageSender.replay();

        template.sendSourceAndReceiveToResult(new StringSource(request), new StringResult());
        messageSender.verify();
    }

}
