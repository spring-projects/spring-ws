/*
 * Copyright 2008 the original author or authors.
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

package org.springframework.ws.server.endpoint.mapping;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class LogAspect {

	private static final Log logger = LogFactory.getLog(LogAspect.class);

	private boolean logInvoked = false;

	public boolean isLogInvoked() {
		return logInvoked;
	}

	@Pointcut("@annotation(org.springframework.ws.server.endpoint.mapping.Log)")
	private void loggedMethod() {

	}

	@Around("loggedMethod()")
	public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
		logInvoked = true;
		logger.info("Before: " + joinPoint.getSignature());
		try {
			return joinPoint.proceed();
		}
		finally {
			logger.info("After: " + joinPoint.getSignature());
		}
	}

}
