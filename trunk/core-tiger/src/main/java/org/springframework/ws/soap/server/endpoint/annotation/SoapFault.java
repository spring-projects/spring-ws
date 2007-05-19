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

package org.springframework.ws.soap.server.endpoint.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an exception class with the fault elements that should be returned whenever this exception is thrown.
 *
 * @author Arjen Poutsma
 * @see org.springframework.ws.soap.server.endpoint.SoapFaultAnnotationExceptionResolver
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface SoapFault {

    /** The fault code. */
    FaultCode faultCode();

    /**
     * The custom fault code, to be used if {@link #faultCode()} is set to {@link FaultCode#CUSTOM}.
     * <p/>
     * Note that custom Fault Codes are only supported on SOAP 1.1.
     */
    String customFaultCode() default "";

    /** The fault string or reason text. By default, it is set to the exception message. */
    String faultStringOrReason() default "";

    /** The fault string locale. By default, it is English. */
    String locale() default "en";


}
