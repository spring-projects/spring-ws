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

package org.springframework.ws.soap.security;

import javax.xml.namespace.QName;

/**
 * Exception indicating that a WS-Security executions should result in a SOAP Fault.
 *
 * @author Arjen Poutsma
 * @since 1.0.1
 */
public abstract class WsSecurityFaultException extends WsSecurityException {

    private QName faultCode;

    private String faultString;

    private String faultActor;

    /** Construct a new {@code WsSecurityFaultException} with the given fault code, string, and actor. */
    public WsSecurityFaultException(QName faultCode, String faultString, String faultActor) {
        super(faultString);
        this.faultCode = faultCode;
        this.faultString = faultString;
        this.faultActor = faultActor;
    }

    /** Returns the fault code for the exception. */
    public QName getFaultCode() {
        return faultCode;
    }

    /** Returns the fault string for the exception. */
    public String getFaultString() {
        return faultString;
    }

    /** Returns the fault actor for the exception. */
    public String getFaultActor() {
        return faultActor;
    }
}
