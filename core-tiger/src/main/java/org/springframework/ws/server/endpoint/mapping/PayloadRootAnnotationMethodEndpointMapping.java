/*
 * Copyright 2007 the original author or authors.
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

package org.springframework.ws.server.endpoint.mapping;

import java.lang.reflect.Method;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerFactory;

import org.springframework.util.StringUtils;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointMapping;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.support.PayloadRootUtils;

/**
 * Implementation of the {@link EndpointMapping} interface that uses the {@link PayloadRoot} annotation to map methods
 * to request payload root elements.
 * <p/>
 * Endpoints typically have the following form:
 * <pre>
 * &#64;Endpoint
 * public class MyEndpoint{
 *    &#64;Payload(localPart = "Request",
 *                 namespace = "http://springframework.org/spring-ws")
 *    public Source doSomethingWithRequest() {
 *       ...
 *    }
 * }
 * </pre>
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public class PayloadRootAnnotationMethodEndpointMapping extends AbstractAnnotationMethodEndpointMapping {

    private static TransformerFactory transformerFactory;

    static {
        transformerFactory = TransformerFactory.newInstance();
    }

    protected String getLookupKeyForMessage(MessageContext messageContext) throws Exception {
        QName qName = PayloadRootUtils
                .getPayloadRootQName(messageContext.getRequest().getPayloadSource(), transformerFactory);
        return qName != null ? qName.toString() : null;
    }

    protected String getLookupKeyForMethod(Method method) {
        PayloadRoot annotation = method.getAnnotation(PayloadRoot.class);
        if (annotation != null) {
            QName qname;
            if (StringUtils.hasLength(annotation.localPart()) && StringUtils.hasLength(annotation.namespace())) {
                qname = new QName(annotation.namespace(), annotation.localPart());
            }
            else {
                qname = new QName(annotation.localPart());
            }
            return qname.toString();
        }
        else {
            return null;
        }
    }

}
