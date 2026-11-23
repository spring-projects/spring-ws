/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.security.wss4j2;

import javax.xml.namespace.QName;

import org.springframework.ws.soap.security.WsSecurityFaultException;

/**
 * WSS4J-specific version of the {@link WsSecurityFaultException}.
 *
 * @author Tareq Abed Rabbo
 * @author Arjen Poutsma
 * @author Jamin Hitchcock
 * @since 2.3.0
 */
@SuppressWarnings("serial")
public class Wss4jSecurityFaultException extends WsSecurityFaultException {

	public Wss4jSecurityFaultException(QName faultCode, String faultString, String faultActor) {
		super(faultCode, faultString, faultActor);
	}

}
