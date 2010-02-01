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

package org.springframework.ws.transport.mail;

import org.custommonkey.xmlunit.XMLAssert;
import org.jvnet.mock_javamail.Mailbox;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

public class MailIntegrationTest extends AbstractDependencyInjectionSpringContextTests {

    private WebServiceTemplate webServiceTemplate;

    @Override
    protected String[] getConfigLocations() {
        return new String[]{"classpath:org/springframework/ws/transport/mail/mail-applicationContext.xml"};
    }

    @Override
    protected void onTearDown() throws Exception {
        Mailbox.clearAll();
    }

    public void setWebServiceTemplate(WebServiceTemplate webServiceTemplate) {
        this.webServiceTemplate = webServiceTemplate;
    }

    public void testMailTransport() throws Exception {
        String content = "<root xmlns='http://springframework.org/spring-ws'><child/></root>";
        StringResult result = new StringResult();
        webServiceTemplate.sendSourceAndReceiveToResult(new StringSource(content), result);
        applicationContext.close();
        assertEquals("Server mail message not deleted", 0, Mailbox.get("server@example.com").size());
        assertEquals("No client mail message received", 1, Mailbox.get("client@example.com").size());
        XMLAssert.assertXMLEqual("Invalid content received", content, result.toString());

    }

}
