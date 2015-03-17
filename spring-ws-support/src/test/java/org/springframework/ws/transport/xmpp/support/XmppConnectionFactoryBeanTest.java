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

package org.springframework.ws.transport.xmpp.support;

import java.io.IOException;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.junit.Before;
import org.junit.Test;

/** @author Arjen Poutsma */
public class XmppConnectionFactoryBeanTest {

    private XmppConnectionFactoryBean factoryBean;

    @Before
    public void createFactoryBean() {
        factoryBean = new XmppConnectionFactoryBean();
    }
    @Test(expected = IllegalArgumentException.class)
    public void noHost() throws XMPPException, SmackException, IOException {
        factoryBean.afterPropertiesSet();
    }

    @Test(expected = IllegalArgumentException.class)
    public void noUsername() throws XMPPException, SmackException, IOException {
        factoryBean.setHost("jabber.org");
        factoryBean.afterPropertiesSet();
    }

    @Test(expected = IllegalArgumentException.class)
    public void wrongPort() throws XMPPException {
        factoryBean.setPort(-10);
    }

}
