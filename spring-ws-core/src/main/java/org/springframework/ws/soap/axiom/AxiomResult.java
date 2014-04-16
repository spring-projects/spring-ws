/*
 * Copyright 2005-2010 the original author or authors.
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

import javax.xml.transform.sax.SAXResult;

import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;

/**
 * Specific TrAX {@link javax.xml.transform.Result} that adds the resulting AXIOM OMElement to a specified parent
 * element when {@code endDocument} is called.
 *
 * @author Arjen Poutsma
 * @see AxiomHandler
 * @since 1.5.0
 */
class AxiomResult extends SAXResult {

    AxiomResult(OMContainer container, OMFactory factory) {
        AxiomHandler handler = new AxiomHandler(container, factory);
        super.setHandler(handler);
        super.setLexicalHandler(handler);
    }

    /**
     * Throws a {@code UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public void setHandler(ContentHandler handler) {
        throw new UnsupportedOperationException("setHandler is not supported");
    }

    /**
     * Throws a {@code UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public void setLexicalHandler(LexicalHandler handler) {
        throw new UnsupportedOperationException("setLexicalHandler is not supported");
    }
}
