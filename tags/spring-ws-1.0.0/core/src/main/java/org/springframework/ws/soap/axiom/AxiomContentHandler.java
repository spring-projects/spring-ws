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

package org.springframework.ws.soap.axiom;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.SAXOMBuilder;
import org.springframework.util.Assert;
import org.xml.sax.SAXException;

/**
 * Specific SAX ContentHandler that adds the resulting AXIOM OMElement to a specified parent element when
 * <code>endDocument</code> is called. Used for returing <code>SAXResult</code>s from Axiom elements.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
class AxiomContentHandler extends SAXOMBuilder {

    private OMElement parentElement = null;

    public AxiomContentHandler(OMElement parentElement) {
        Assert.notNull(parentElement, "No parentElement given");
        this.parentElement = parentElement;
    }

    public void endDocument() throws SAXException {
        super.endDocument();
        parentElement.addChild(super.getRootElement());
    }
}
