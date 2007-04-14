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

package org.springframework.ws.server.endpoint;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;

import org.jdom.Element;
import org.jdom.transform.JDOMResult;
import org.jdom.transform.JDOMSource;
import org.springframework.xml.transform.TransformerObjectSupport;

/**
 * Abstract base class for endpoints that handle the message payload as JDOM elements.
 *
 * <p>Offers the message payload as a JDOM {@link Element}, and allows subclasses to create a response by returning an
 * <code>Element</code>.
 *
 * <pAn <code>AbstractJDomPayloadEndpoint</code> can accept only <i>one</i> payload element. Multiple payload elements
 * are not in accordance with WS-I.
 *
 * @author Arjen Poutsma
 */
public abstract class AbstractJDomPayloadEndpoint extends TransformerObjectSupport implements PayloadEndpoint {

    public final Source invoke(Source request) throws Exception {
        Transformer transformer = createTransformer();
        JDOMResult jdomResult = new JDOMResult();
        transformer.transform(request, jdomResult);
        Element requestElement = jdomResult.getDocument().getRootElement();
        Element responseElement = invokeInternal(requestElement);
        return responseElement != null ? new JDOMSource(responseElement) : null;
    }

    /**
     * Template method. Subclasses must implement this. Offers the request payload as a JDOM <code>Element</code>, and
     * allows subclasses to return a response <code>Element</code>.
     *
     * @param requestElement the contents of the SOAP message as JDOM element
     * @return the response element. Can be <code>null</code> to specify no response.
     */
    protected abstract Element invokeInternal(Element requestElement) throws Exception;
}
