/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.server;

import org.springframework.ws.server.SmartEndpointInterceptor;

/**
 * SOAP-specific extension of the
 * {@link org.springframework.ws.server.SmartEndpointInterceptor} interface. Allows for
 * handling of SOAP faults, which are considered different from regular responses.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public interface SmartSoapEndpointInterceptor extends SmartEndpointInterceptor, SoapEndpointInterceptor {

}
