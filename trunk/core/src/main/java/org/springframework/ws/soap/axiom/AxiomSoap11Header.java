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

package org.springframework.ws.soap.axiom;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.axiom.soap.RolePlayer;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeader;
import org.springframework.util.ObjectUtils;
import org.springframework.ws.soap.soap11.Soap11Header;

/**
 * Axiom-specific version of <code>org.springframework.ws.soap.Soap11Header</code>.
 *
 * @author Arjen Poutsma
 * @since 1.0
 */
class AxiomSoap11Header extends AxiomSoapHeader implements Soap11Header {

    AxiomSoap11Header(SOAPHeader axiomHeader, SOAPFactory axiomFactory) {
        super(axiomHeader, axiomFactory);
    }

    public Iterator examineHeaderElementsToProcess(final String[] actors) {
        RolePlayer rolePlayer = null;
        if (!ObjectUtils.isEmpty(actors)) {
            rolePlayer = new RolePlayer() {

                public List getRoles() {
                    return Arrays.asList(actors);
                }

                public boolean isUltimateDestination() {
                    return false;
                }
            };
        }
        Iterator result = getAxiomHeader().getHeadersToProcess(rolePlayer);
        return new AxiomSoapHeaderElementIterator(result);
    }
}
