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

package org.springframework.ws.soap.saaj;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.springframework.ws.soap.SoapBody;

/**
 * SAAJ-specific abstract base class of the <code>SoapBody</code> interface. Wraps a {@link javax.xml.soap.SOAPBody}.
 *
 * @author Arjen Poutsma
 */
abstract class SaajSoapBody extends SaajSoapElement implements SoapBody {

    public SaajSoapBody(SOAPBody body) {
        super(body);
    }

    public Source getPayloadSource() {
        SOAPBodyElement bodyElement = getImplementation().getFirstBodyElement(getSaajBody());
        return bodyElement == null ? null : getImplementation().getSource(bodyElement);
    }

    public Result getPayloadResult() {
        getImplementation().removeContents(getSaajBody());
        return getImplementation().getResult(getSaajBody());
    }

    public boolean hasFault() {
        return getImplementation().hasFault(getSaajBody());
    }

    protected SOAPBody getSaajBody() {
        return (SOAPBody) getSaajElement();
    }

}
