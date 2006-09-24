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

package org.springframework.ws.pox.dom;

import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;

import org.springframework.util.Assert;
import org.springframework.ws.pox.PoxMessage;
import org.springframework.ws.pox.context.AbstractPoxMessageContext;
import org.springframework.ws.transport.TransportRequest;
import org.springframework.ws.transport.TransportResponse;
import org.w3c.dom.Document;

/**
 * Implementation of the <code>MessageContext</code> that contains a <code>DomPoxMessage</code>.
 *
 * @author Arjen Poutsma
 * @see DomPoxMessage
 */
public class DomPoxMessageContext extends AbstractPoxMessageContext {

    private DocumentBuilder documentBuilder;

    private Transformer transformer;

    /**
     * Creates a new <code>DomPoxMessageContext</code> with the given parameters.
     */
    public DomPoxMessageContext(Document request,
                                TransportRequest transportRequest,
                                DocumentBuilder documentBuilder,
                                Transformer transformer) {
        super(new DomPoxMessage(request, transformer), transportRequest);
        Assert.notNull(documentBuilder, "documentBuilder must not be null");
        Assert.notNull(transformer, "transformer must not be null");
        this.documentBuilder = documentBuilder;
        this.transformer = transformer;
    }

    protected PoxMessage createResponsePoxMessage() {
        Document document = documentBuilder.newDocument();
        return new DomPoxMessage(document, transformer);
    }

    public void sendResponse(TransportResponse transportResponse) throws IOException {
        if (hasResponse()) {
            transportResponse.addHeader("Content-Type", "text/xml");
            getResponse().writeTo(transportResponse.getOutputStream());
        }
    }
}
