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

import java.io.IOException;
import javax.activation.DataHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.mime.MimeContainer;
import org.springframework.oxm.mime.MimeMarshaller;
import org.springframework.oxm.mime.MimeUnmarshaller;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.mime.Attachment;
import org.springframework.ws.mime.MimeMessage;

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

    /** Logger available to subclasses. */
    protected final Log logger = LogFactory.getLog(getClass());

    private Marshaller marshaller;

    private Unmarshaller unmarshaller;

    /**
     * Creates a new <code>AbstractMarshallingPayloadEndpoint</code>. The {@link Marshaller} and {@link Unmarshaller}
     * must be injected using properties.
     *
     * @see #setMarshaller(org.springframework.oxm.Marshaller)
     * @see #setUnmarshaller(org.springframework.oxm.Unmarshaller)
     */
    protected AbstractMarshallingPayloadEndpoint() {
    }

    /**
     * Creates a new <code>AbstractMarshallingPayloadEndpoint</code> with the given marshaller and unmarshaller.
     *
     * @param marshaller   the marshaller to use
     * @param unmarshaller the unmarshaller to use
     */
    protected AbstractMarshallingPayloadEndpoint(Marshaller marshaller, Unmarshaller unmarshaller) {
        this.marshaller = marshaller;
        this.unmarshaller = unmarshaller;
    }

    /** Returns the marshaller used for transforming objects into XML. */
    public final Marshaller getMarshaller() {
        return marshaller;
    }

    /** Sets the marshaller used for transforming objects into XML. */
    public final void setMarshaller(Marshaller marshaller) {
        this.marshaller = marshaller;
    }

    /** Returns the unmarshaller used for transforming XML into objects. */
    public final Unmarshaller getUnmarshaller() {
        return unmarshaller;
    }

    /** Sets the unmarshaller used for transforming XML into objects. */
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
        Object requestObject = unmarshalRequest(request);
        Object responseObject = invokeInternal(requestObject);
        if (responseObject != null) {
            WebServiceMessage response = messageContext.getResponse();
            marshalResponse(responseObject, response);
        }
    }

    private Object unmarshalRequest(WebServiceMessage request) throws IOException {
        Object requestObject;
        if (unmarshaller instanceof MimeUnmarshaller && request instanceof MimeMessage) {
            MimeUnmarshaller mimeUnmarshaller = (MimeUnmarshaller) unmarshaller;
            MimeMessageContainer container = new MimeMessageContainer((MimeMessage) request);
            requestObject = mimeUnmarshaller.unmarshal(request.getPayloadSource(), container);
        }
        else {
            requestObject = unmarshaller.unmarshal(request.getPayloadSource());
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Unmarshalled payload request to [" + requestObject + "]");
        }
        return requestObject;
    }

    private void marshalResponse(Object responseObject, WebServiceMessage response) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("Marshalling [" + responseObject + "] to response payload");
        }
        if (marshaller instanceof MimeMarshaller && response instanceof MimeMessage) {
            MimeMarshaller mimeMarshaller = (MimeMarshaller) marshaller;
            MimeMessageContainer container = new MimeMessageContainer((MimeMessage) response);
            mimeMarshaller.marshal(responseObject, response.getPayloadResult(), container);
        }
        else {
            marshaller.marshal(responseObject, response.getPayloadResult());
        }
    }

    /**
     * Template method that gets called after the marshaller and unmarshaller have been set.
     * <p/>
     * The default implementation does nothing.
     */
    public void afterMarshallerSet() throws Exception {
    }

    /**
     * Template method that subclasses must implement to process a request.
     * <p/>
     * The unmarshaled request object is passed as a parameter, and an the returned object is marshalled to a response.
     * If no response is required, return <code>null</code>.
     *
     * @param requestObject the unnmarshalled message payload as object
     * @return the object to be marshalled as response, or <code>null</code> if a response is not required
     */
    protected abstract Object invokeInternal(Object requestObject) throws Exception;

    private static class MimeMessageContainer implements MimeContainer {

        private final MimeMessage mimeMessage;

        public MimeMessageContainer(MimeMessage mimeMessage) {
            this.mimeMessage = mimeMessage;
        }

        public boolean isXopPackage() {
            return mimeMessage.isXopPackage();
        }

        public void addAttachment(String contentId, DataHandler dataHandler) {
            mimeMessage.addAttachment(contentId, dataHandler);
        }

        public DataHandler getAttachment(String contentId) {
            Attachment attachment = mimeMessage.getAttachment(contentId);
            return attachment.getDataHandler();
        }
    }
}
