/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.server.endpoint;

import java.util.Locale;

import javax.xml.namespace.QName;

import org.jspecify.annotations.Nullable;

import org.springframework.util.StringUtils;
import org.springframework.ws.soap.server.endpoint.annotation.FaultCode;
import org.springframework.ws.soap.server.endpoint.annotation.SoapFault;

/**
 * Implementation of the {@link org.springframework.ws.server.EndpointExceptionResolver}
 * interface that uses the {@link SoapFault} annotation to map exceptions to SOAP Faults.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public class SoapFaultAnnotationExceptionResolver extends AbstractSoapFaultDefinitionExceptionResolver {

	@Override
	protected final @Nullable SoapFaultDefinition getFaultDefinition(@Nullable Object endpoint, Exception ex) {
		SoapFault faultAnnotation = ex.getClass().getAnnotation(SoapFault.class);
		if (faultAnnotation != null) {
			SoapFaultDefinition definition = new SoapFaultDefinition();
			if (faultAnnotation.faultCode() != FaultCode.CUSTOM) {
				definition.setFaultCode(faultAnnotation.faultCode().value());
			}
			else if (StringUtils.hasLength(faultAnnotation.customFaultCode())) {
				definition.setFaultCode(QName.valueOf(faultAnnotation.customFaultCode()));
			}
			definition.setFaultStringOrReason(faultAnnotation.faultStringOrReason());
			Locale locale = StringUtils.parseLocaleString(faultAnnotation.locale());
			if (locale != null) {
				definition.setLocale(locale);
			}
			return definition;
		}
		else {
			return null;
		}
	}

}
