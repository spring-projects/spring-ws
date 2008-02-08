/*
 * Copyright 2008 the original author or authors.
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

package org.springframework.ws.soap.security.wss4j;

import java.util.Vector;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Document;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.axiom.AxiomSoapMessage;
import org.springframework.ws.soap.axiom.support.AxiomUtils;
import org.springframework.ws.soap.saaj.SaajSoapMessage;

/**
 * @author Tareq Abed Rabbo
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public abstract class AbstractWss4jInterceptor implements EndpointInterceptor, InitializingBean {

    protected final Log logger = LogFactory.getLog(getClass());

    protected static final QName WS_SECURITY_NAME =
            new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Security");

    private Vector actions;

    private int actionFlags;

    private String actor;

    protected Vector getActions() {
        return actions;
    }

    protected int getActionFlags() {
        return actionFlags;
    }

    public final void setActions(String actions) throws WSSecurityException {
        this.actions = new Vector();
        this.actionFlags = WSSecurityUtil.decodeAction(actions, this.actions);
    }

    protected String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(actions, "'actions' must not be null");
    }

    /**
     * Transforms a soap message to a DOM document.
     *
     * @param soapMessage the message to transform
     * @return a DOM document representing the message
     */
    protected Document toDocument(SoapMessage soapMessage) {
        if (soapMessage instanceof SaajSoapMessage) {
            SaajSoapMessage saajMessage = (SaajSoapMessage) soapMessage;
            return saajMessage.getSaajMessage().getSOAPPart();
        }
        else if (soapMessage instanceof AxiomSoapMessage) {
            AxiomSoapMessage axiomMessage = (AxiomSoapMessage) soapMessage;
            return AxiomUtils.toDocument(axiomMessage.getAxiomMessage().getSOAPEnvelope());
        }
        else {
            throw new IllegalArgumentException("Unknown SoapMessage implementation [" + soapMessage + "]");
        }
    }
}
