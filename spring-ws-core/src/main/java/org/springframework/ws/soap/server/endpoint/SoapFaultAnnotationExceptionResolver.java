/*
 * Copyright 2005-2022 the original author or authors.
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

package org.springframework.ws.soap.server.endpoint;

import javax.xml.namespace.QName;

import org.springframework.util.StringUtils;
import org.springframework.ws.soap.server.endpoint.annotation.FaultCode;
import org.springframework.ws.soap.server.endpoint.annotation.SoapFault;

/**
 * Implementation of the {@link org.springframework.ws.server.EndpointExceptionResolver} interface that uses the
 * {@link SoapFault} annotation to map exceptions to SOAP Faults.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public class SoapFaultAnnotationExceptionResolver extends AbstractSoapFaultDefinitionExceptionResolver {

	@Override
	protected final SoapFaultDefinition getFaultDefinition(Object endpoint, Exception ex) {
		SoapFault faultAnnotation = ex.getClass().getAnnotation(SoapFault.class);
		if (faultAnnotation != null) {
			SoapFaultDefinition definition = new SoapFaultDefinition();
			if (faultAnnotation.faultCode() != FaultCode.CUSTOM) {
				definition.setFaultCode(faultAnnotation.faultCode().value());
			} else if (StringUtils.hasLength(faultAnnotation.customFaultCode())) {
				definition.setFaultCode(QName.valueOf(faultAnnotation.customFaultCode()));
			}
			definition.setFaultStringOrReason(faultAnnotation.faultStringOrReason());
			definition.setLocale(StringUtils.parseLocaleString(faultAnnotation.locale()));
			return definition;
		} else {
			return null;
		}
	}
}
