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

import java.util.Iterator;
import java.util.Locale;
import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

/**
 * Defines the contract for different implementation versions of SAAJ.
 * <p/>
 * This is pulled out into an interface as the various versions of SAAJ have different contracts with regard to naming,
 * etc.
 *
 * @author Arjen Poutsma
 */
public interface SaajImplementationStrategy {

    public QName getName(SOAPElement element);

    public Source getSource(SOAPElement element);

    public Result getResult(SOAPElement element);

    public QName getFaultCode(SOAPFault fault);

    public DetailEntry addDetailEntry(Detail detail, QName name) throws SOAPException;

    public void removeContents(SOAPElement element);

    public SOAPHeaderElement addHeaderElement(SOAPHeader header, QName name) throws SOAPException;

    public Locale getFaultStringLocale(SOAPFault saajFault);

    public Iterator examineMustUnderstandHeaderElements(SOAPHeader header, String role);

    SOAPFault addFault(SOAPBody saajBody, QName faultCode, String faultString, Locale faultStringLocale) throws SOAPException;
}
