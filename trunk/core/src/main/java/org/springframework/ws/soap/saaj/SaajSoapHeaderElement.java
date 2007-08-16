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

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.transform.Result;

import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapHeaderException;

/**
 * SAAJ-specific implementation of the <code>SoapHeaderElement</code> interface. Wraps a {@link
 * javax.xml.soap.SOAPHeaderElement}.
 *
 * @author Arjen Poutsma
 * @since 1.0
 */
class SaajSoapHeaderElement extends SaajSoapElement implements SoapHeaderElement {

    SaajSoapHeaderElement(SOAPHeaderElement headerElement) {
        super(headerElement);
    }

    public Result getResult() throws SoapHeaderException {
        return getImplementation().getResult(getSaajElement());
    }

    public String getActorOrRole() throws SoapHeaderException {
        return getImplementation().getActorOrRole(getSaajHeaderElement());
    }

    public void setActorOrRole(String actorOrRole) throws SoapHeaderException {
        getImplementation().setActorOrRole(getSaajHeaderElement(), actorOrRole);
    }

    public boolean getMustUnderstand() throws SoapHeaderException {
        return getImplementation().getMustUnderstand(getSaajHeaderElement());
    }

    public void setMustUnderstand(boolean mustUnderstand) throws SoapHeaderException {
        getImplementation().setMustUnderstand(getSaajHeaderElement(), mustUnderstand);
    }

    public String getText() {
        return getImplementation().getText(getSaajHeaderElement());
    }

    public void setText(String content) {
        try {
            getImplementation().setText(getSaajHeaderElement(), content);
        }
        catch (SOAPException ex) {
            throw new SaajSoapHeaderException(ex);
        }
    }

    protected SOAPHeaderElement getSaajHeaderElement() {
        return (SOAPHeaderElement) getSaajElement();
    }

}
