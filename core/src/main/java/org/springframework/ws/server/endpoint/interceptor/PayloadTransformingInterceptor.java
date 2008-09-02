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

package org.springframework.ws.server.endpoint.interceptor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.xml.transform.ResourceSource;
import org.springframework.xml.transform.TransformerObjectSupport;

/**
 * Interceptor that transforms the payload of <code>WebServiceMessage</code>s using XSLT stylesheet. Allows for seperate
 * stylesheets for request and response. This interceptor is especially useful when supporting with multiple version of
 * a Web service: you can transform the older message format to the new format.
 * <p/>
 * The stylesheets to use can be set using the <code>requestXslt</code> and <code>responseXslt</code> properties. Both
 * of these are optional: if not set, the message is simply not transformed. Setting one of the two is required,
 * though.
 *
 * @author Arjen Poutsma
 * @see #setRequestXslt(org.springframework.core.io.Resource)
 * @see #setResponseXslt(org.springframework.core.io.Resource)
 * @since 1.0.0
 */
public class PayloadTransformingInterceptor extends TransformerObjectSupport
        implements EndpointInterceptor, InitializingBean {

    private static final Log logger = LogFactory.getLog(PayloadTransformingInterceptor.class);

    private Resource requestXslt;

    private Resource responseXslt;

    private Templates requestTemplates;

    private Templates responseTemplates;

    /** Sets the XSLT stylesheet to use for transforming incoming request. */
    public void setRequestXslt(Resource requestXslt) {
        this.requestXslt = requestXslt;
    }

    /** Sets the XSLT stylesheet to use for transforming outgoing responses. */
    public void setResponseXslt(Resource responseXslt) {
        this.responseXslt = responseXslt;
    }

    /**
     * Transforms the request message in the given message context using a provided stylesheet. Transformation only
     * occurs if the <code>requestXslt</code> has been set.
     *
     * @param messageContext the message context
     * @return always returns <code>true</code>
     * @see #setRequestXslt(org.springframework.core.io.Resource)
     */
    public boolean handleRequest(MessageContext messageContext, Object endpoint) throws Exception {
        if (requestTemplates != null) {
            WebServiceMessage request = messageContext.getRequest();
            Transformer transformer = requestTemplates.newTransformer();
            transformMessage(request, transformer);
            logger.debug("Request message transformed");
        }
        return true;
    }

    /**
     * Transforms the response message in the given message context using a stylesheet. Transformation only occurs if
     * the <code>responseXslt</code> has been set.
     *
     * @param messageContext the message context
     * @return always returns <code>true</code>
     * @see #setResponseXslt(org.springframework.core.io.Resource)
     */
    public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
        if (responseTemplates != null) {
            WebServiceMessage response = messageContext.getResponse();
            Transformer transformer = responseTemplates.newTransformer();
            transformMessage(response, transformer);
            logger.debug("Response message transformed");
        }
        return true;
    }

    private void transformMessage(WebServiceMessage message, Transformer transformer) throws TransformerException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        transformer.transform(message.getPayloadSource(), new StreamResult(os));
        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        transform(new StreamSource(is), message.getPayloadResult());
    }

    /** Does nothing by default. Faults are not transformed. */
    public boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
        return true;
    }

    public void afterPropertiesSet() throws Exception {
        if (requestXslt == null && responseXslt == null) {
            throw new IllegalArgumentException("Setting either 'requestXslt' or 'responseXslt' is required");
        }
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        xmlReader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
        if (requestXslt != null) {
            Assert.isTrue(requestXslt.exists(), "requestXslt \"" + requestXslt + "\" does not exit");
            if (logger.isInfoEnabled()) {
                logger.info("Transforming request using " + requestXslt);
            }
            Source requestSource = new ResourceSource(xmlReader, requestXslt);
            requestTemplates = transformerFactory.newTemplates(requestSource);
        }
        if (responseXslt != null) {
            Assert.isTrue(responseXslt.exists(), "responseXslt \"" + responseXslt + "\" does not exit");
            if (logger.isInfoEnabled()) {
                logger.info("Transforming response using " + responseXslt);
            }
            Source responseSource = new ResourceSource(xmlReader, responseXslt);
            responseTemplates = transformerFactory.newTemplates(responseSource);
        }
    }
}
