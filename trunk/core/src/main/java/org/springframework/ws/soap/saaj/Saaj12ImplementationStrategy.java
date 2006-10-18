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
 *  * limitations under the License.
 */

package org.springframework.ws.soap.saaj;

import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.springframework.ws.soap.saaj.support.SaajUtils;

class Saaj12ImplementationStrategy extends SaajImplementationStrategy {

    public Source getSource(SOAPElement element) {
        return new DOMSource(element);
    }

    public Result getResult(SOAPElement element) {
        return new DOMResult(element);
    }

    public QName getName(SOAPElement element) {
        return SaajUtils.toQName(element.getElementName());
    }

    public QName getFaultCode(SOAPFault fault) {
        return SaajUtils.toQName(fault.getFaultCodeAsName());
    }

    public DetailEntry addDetailEntry(Detail detail, QName name) throws SOAPException {
        return detail.addDetailEntry(SaajUtils.toName(name, detail));
    }

    public void removeContents(SOAPElement element) {
        element.removeContents();
    }
}
