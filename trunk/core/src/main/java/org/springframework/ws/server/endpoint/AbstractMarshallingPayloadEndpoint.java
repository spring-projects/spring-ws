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

package org.springframework.ws.server.endpoint;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;

/**
 * Endpoint that unmarshals the request payload, and marshals the response object. This endpoint needs a
 * <code>Marshaller</code> and <code>Unmarshaller</code>, both of which can be set using properties. An abstract
 * template method is invoked using the request object as a parameter, and allows for a response object to be returned.
 *
 * @author Arjen Poutsma
 * @see #setMarshaller(org.springframework.oxm.Marshaller)
 * @see Marshaller
 * @see #setUnmarshaller(org.springframework.oxm.Unmarshaller)
 * @see Unmarshaller
 * @see #invokeInternal(Object)
 */
public abstract class AbstractMarshallingPayloadEndpoint implements MessageEndpoint, InitializingBean {

    /**
     * Logger available to subclasses.
     */
    protected final Log logger = LogFactory.getLog(getClass());

    private Marshaller marshaller;

    private Unmarshaller unmarshaller;

    /**
     * Returns the marshaller used for transforming objects into XML.
     */
    public final Marshaller getMarshaller() {
        return marshaller;
    }

    /**
     * Sets the marshaller used for transforming objects into XML.
     */
    public final void setMarshaller(Marshaller marshaller) {
        this.marshaller = marshaller;
    }

    /**
     * Returns the unmarshaller used for transforming XML into objects.
     */
    public final Unmarshaller getUnmarshaller() {
        return unmarshaller;
    }

    /**
     * Sets the unmarshaller used for transforming XML into objects.
     */
    public final void setUnmarshaller(Unmarshaller unmarshaller) {
        this.unmarshaller = unmarshaller;
    }

    public final void afterPropertiesSet() throws Exception {
        Assert.notNull(marshaller, "marshaller is required");
        Assert.notNull(unmarshaller, "unmarshaller is required");
        afterMarshallerSet();
    }

    public final void invoke(MessageContext messageContext) throws Exception {
        WebServiceMessage request = messageContext.getRequest();
        Object requestObject = unmarshaller.unmarshal(request.getPayloadSource());
        if (logger.isDebugEnabled()) {
            logger.debug("Unmarshalled payload request to [" + requestObject + "]");
        }
        Object responseObject = invokeInternal(requestObject);
        if (responseObject != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Marshalling [" + responseObject + "] to response payload");
            }
            WebServiceMessage response = messageContext.getResponse();
            marshaller.marshal(responseObject, response.getPayloadResult());
        }
    }

    /**
     * Template method that gets called after the marshaller and unmarshaller have been set.
     *
     * <p>The default implementation does nothing.
     */
    public void afterMarshallerSet() throws Exception {
    }

    /**
     * Template method that subclasses must implement to process a request.
     *
     * <p>The unmarshaled request object is passed as a parameter, and an the returned object is marshalled to a
     * response. If no response is required, return <code>null</code>.
     *
     * @param requestObject the unnmarshalled message payload as object
     * @return the object to be marshalled as response, or <code>null</code> if a response is not required
     */
    protected abstract Object invokeInternal(Object requestObject) throws Exception;
}
