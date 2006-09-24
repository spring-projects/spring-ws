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

package org.springframework.ws.soap.saaj;

import javax.xml.soap.MessageFactory;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Spring <code>FactoryBean</code> for SAAJ <code>MessageFactory</code> objects.
 *
 * @author Arjen Poutsma
 * @see javax.xml.soap.MessageFactory
 */
public class SaajMessageFactoryBean implements FactoryBean, InitializingBean {

    private MessageFactory messageFactory;

    public Object getObject() throws Exception {
        return messageFactory;
    }

    public Class getObjectType() {
        return MessageFactory.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void afterPropertiesSet() throws Exception {
        messageFactory = MessageFactory.newInstance();
    }
}
