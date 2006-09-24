/*
 * Copyright 2005 the original author or authors.
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

package org.springframework.ws.endpoint;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;

/**
 * Endpoint that unmarshals the request payload, and marshals the response object.
 *
 * @author Arjen Poutsma
 */
public abstract class AbstractMarshallingPayloadEndpoint implements MessageEndpoint, InitializingBean {

    protected final Log logger = LogFactory.getLog(getClass());

    private Marshaller marshaller;

    private Unmarshaller unmarshaller;

    public final void invoke(MessageContext messageContext) throws Exception {
        WebServiceMessage request = messageContext.getRequest();
        Object requestObject = this.unmarshaller.unmarshal(request.getPayloadSource());
        if (logger.isDebugEnabled()) {
            logger.debug("Unmarshalled payload request to [" + requestObject + "]");
        }
        Object responseObject = invokeInternal(requestObject);
        if (responseObject != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Marshalling [" + responseObject + "] to response payload");
            }
            WebServiceMessage response = messageContext.createResponse();
            marshaller.marshal(responseObject, response.getPayloadResult());
        }
    }

    /**
     * Template method. Subclasses must implement this.
     *
     * @param requestObject the unnmarshalled message payload as object
     * @return the object to be marshalled as response
     */
    protected abstract Object invokeInternal(Object requestObject) throws Exception;

    public final void setMarshaller(Marshaller marshaller) {
        this.marshaller = marshaller;
    }

    public final void setUnmarshaller(Unmarshaller unmarshaller) {
        this.unmarshaller = unmarshaller;
    }

    public final void afterPropertiesSet() throws Exception {
        Assert.notNull(marshaller, "marshaller is required");
        Assert.notNull(unmarshaller, "unmarshaller is required");
        afterMarshallerSet();
    }

    /**
     * Template method that gets called after the marshaller and unmarshaller have been set.
     */
    public void afterMarshallerSet() throws Exception {
    }

}
