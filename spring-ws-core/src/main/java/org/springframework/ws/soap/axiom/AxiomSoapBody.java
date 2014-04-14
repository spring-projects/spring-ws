/*
 * Copyright 2005-2014 the original author or authors.
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

package org.springframework.ws.soap.axiom;

import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPFactory;

import org.springframework.util.Assert;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.axiom.support.AxiomUtils;
import org.springframework.ws.stream.StreamingPayload;

/**
 * Axiom-specific version of <code>org.springframework.ws.soap.Soap11Body</code>.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
abstract class AxiomSoapBody extends AxiomSoapElement implements SoapBody {

    private final Payload payload;

    protected AxiomSoapBody(SOAPBody axiomBody, SOAPFactory axiomFactory, boolean payloadCaching) {
        super(axiomBody, axiomFactory);
        if (payloadCaching) {
            payload = new CachingPayload(axiomBody, axiomFactory);
        }
        else {
            payload = new NonCachingPayload(axiomBody, axiomFactory);
        }
    }

    @Override
    public Source getPayloadSource() {
        return payload.getSource();
    }

    @Override
    public Result getPayloadResult() {
        return payload.getResult();
    }

    @Override
    public boolean hasFault() {
        return getAxiomBody().hasFault();
    }

    protected final SOAPBody getAxiomBody() {
        return (SOAPBody) getAxiomElement();
    }

    public void setStreamingPayload(StreamingPayload payload) {
        Assert.notNull(payload, "'payload' must not be null");
        OMDataSource dataSource = new StreamingOMDataSource(payload);
        OMElement payloadElement = getAxiomFactory().createOMElement(dataSource, payload.getName());

        SOAPBody soapBody = getAxiomBody();
        AxiomUtils.removeContents(soapBody);
        soapBody.addChild(payloadElement);
    }
}
