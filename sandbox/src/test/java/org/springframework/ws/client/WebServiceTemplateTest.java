/*
 * Copyright 2006 the original author or authors.
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

package org.springframework.ws.client;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;

public class WebServiceTemplateTest extends TestCase {

    private WebServiceTemplate template;

    private MockControl marshallerControl;

    private Marshaller marshallerMock;

    private MockControl unmarshallerControl;

    private Unmarshaller unmarshallerMock;

    protected void setUp() throws Exception {
        template = new WebServiceTemplate();
        marshallerControl = MockControl.createControl(Marshaller.class);
        marshallerMock = (Marshaller) marshallerControl.getMock();
        template.setMarshaller(marshallerMock);
        unmarshallerControl = MockControl.createControl(Unmarshaller.class);
        unmarshallerMock = (Unmarshaller) unmarshallerControl.getMock();
        template.setUnmarshaller(unmarshallerMock);
    }

    public void testMarshalAndSendNoMarshallerSet() throws Exception {
        template.setMarshaller(null);
        try {
            template.marshalAndSend(new Object());
            fail("IllegalStateException expected");
        }
        catch (IllegalStateException ex) {
            // expected behavior
        }
    }

    public void testMarshalAndSendNoUnmarshallerSet() throws Exception {
        template.setUnmarshaller(null);
        try {
            template.marshalAndSend(new Object());
            fail("IllegalStateException expected");
        }
        catch (IllegalStateException ex) {
            // expected behavior
        }
    }
}