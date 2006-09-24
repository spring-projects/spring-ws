/*
 * Copyright 2005 the original author or authors.
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

import java.io.IOException;
import java.io.OutputStream;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.springframework.ws.soap.SoapMessage;
import org.w3c.dom.Element;

/**
 * SAAJ-specific implementation of the <code>SoapMessage</code> interface. Accessed via the
 * <code>SaajSoapMessageContext</code>.
 * <p/>
 * Uses a <code>SaajMessageHelper</code> underneath. Can either be created with a <code>SaajMessageHelper</code>
 * instance or with a SAAJ <code>SOAPMessage</code> instance.
 *
 * @author Arjen Poutsma
 * @see SaajMessageHelper
 * @see javax.xml.soap.SOAPMessage
 */
public class SaajSoapMessage implements SoapMessage {

    private final SaajMessageHelper helper;

    /**
     * Create a new SaajSoapMessage based on the given SaajMessageHelper.
     *
     * @param saajMessageHelper the SaajMessageHelper
     */
    public SaajSoapMessage(SaajMessageHelper saajMessageHelper) {
        this.helper = saajMessageHelper;
    }

    /**
     * Create a new SaajSoapMessage based on the given SAAJ SOAPMessage.
     *
     * @param soapMessage the SAAJ SOAPMessage
     */
    public SaajSoapMessage(SOAPMessage soapMessage) {
        this.helper = new SaajMessageHelper(soapMessage);
    }

    /**
     * Return the SaajMessageHelper that this SaajSoapMessage is based on.
     */
    public final SaajMessageHelper getSaajMessageHelper() {
        return this.helper;
    }

    /**
     * Return the SAAJ SOAPMessage that this SaajSoapMessage is based on.
     */
    public final SOAPMessage getSaajMessage() {
        return this.helper.getSaajMessage();
    }

    public Source getPayloadSource() {
        try {
            return this.helper.getPayloadSource();
        }
        catch (SOAPException ex) {
            throw new SaajSoapMessageBodyException(ex);
        }
    }

    public Result getPayloadResult() {
        try {
            return this.helper.getPayloadResult();
        }
        catch (SOAPException ex) {
            throw new SaajSoapMessageBodyException(ex);
        }
    }

    public void writeTo(OutputStream outputStream) throws IOException {
        try {
            this.helper.writeTo(outputStream);
        }
        catch (SOAPException ex) {
            throw new SaajSoapMessageNotReadableException(ex);
        }
    }

    public Element getHeader() {
        try {
            return this.helper.getHeader();
        }
        catch (SOAPException ex) {
            throw new SaajSoapMessageHeaderException(ex);
        }
    }

    public Element[] getHeaderElements(QName qName) {
        try {
            return this.helper.getHeaderElements(qName);
        }
        catch (SOAPException ex) {
            throw new SaajSoapMessageHeaderException(ex);
        }
    }

    public Element[] getHeaderElements() {
        try {
            return this.helper.getHeaderElements();
        }
        catch (SOAPException ex) {
            throw new SaajSoapMessageHeaderException(ex);
        }
    }

    public Element[] getMustUnderstandHeaderElements(String actor) {
        try {
            return this.helper.getMustUnderstandHeaderElements(actor);
        }
        catch (SOAPException ex) {
            throw new SaajSoapMessageHeaderException(ex);
        }
    }

    public Element getFault() {
        try {
            return this.helper.getFault();
        }
        catch (SOAPException ex) {
            throw new SaajSoapMessageFaultException(ex);
        }
    }

    public String getSoapAction() {
        return this.helper.getSoapAction();
    }

    public Element addHeaderElement(QName qName, boolean mustUnderstand, String actor) {
        try {
            return this.helper.addHeaderElement(qName, mustUnderstand, actor);
        }
        catch (SOAPException ex) {
            throw new SaajSoapMessageHeaderException(ex);
        }
    }

    public Element addFault(QName faultCode, String faultString, String faultActor) {
        try {
            return this.helper.addFault(faultCode, faultString, faultActor);
        }
        catch (SOAPException ex) {
            throw new SaajSoapMessageFaultException(ex);
        }
    }
}
