package org.springframework.ws.soap.security.xwss;

import javax.xml.namespace.QName;

import org.springframework.ws.soap.security.WsSecurityFaultException;

/**
 * XWSS-specific version of the {@link WsSecurityFaultException}.
 *
 * @author Arjen Poutsma
 * @since 1.0.1
 */
public class XwsSecurityFaultException extends WsSecurityFaultException {

    public XwsSecurityFaultException(QName faultCode, String faultString, String faultActor) {
        super(faultCode, faultString, faultActor);
    }
}
