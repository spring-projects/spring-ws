/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
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
		return this.logInvoked;
	}

	@Pointcut("@annotation(org.springframework.ws.server.endpoint.mapping.Log)")
	private void loggedMethod() {

	}

	@Around("loggedMethod()")
	public Object log(ProceedingJoinPoint joinPoint) throws Throwable {

		this.logInvoked = true;
		logger.info("Before: " + joinPoint.getSignature());

		try {
			return joinPoint.proceed();
		}
		finally {
			logger.info("After: " + joinPoint.getSignature());
		}
	}

}
