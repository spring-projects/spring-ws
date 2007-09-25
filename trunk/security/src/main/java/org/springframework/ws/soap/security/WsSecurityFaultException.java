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

    /**
     * Construct a new <code>WsSecurityFaultException</code> with the given fault code, string, and actor.
     */
    public WsSecurityFaultException(QName faultCode, String faultString, String faultActor) {
        super(faultString);
        this.faultCode = faultCode;
        this.faultString = faultString;
        this.faultActor = faultActor;
    }

    /**
     * Returns the fault code for the exception.
     */
    public QName getFaultCode() {
        return faultCode;
    }

    /**
     * Returns the fault string for the exception.
     */
    public String getFaultString() {
        return faultString;
    }

    /**
     * Returns the fault actor for the exception.
     */
    public String getFaultActor() {
        return faultActor;
    }
}
