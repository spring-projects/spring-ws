/*
 * Copyright 2006 the original author or authors.
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

package org.springframework.ws.soap.security.wss4j.callback.acegi;

import java.lang.reflect.Method;
import java.util.Vector;

import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSUsernameTokenPrincipal;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.handler.WSHandlerResult;
import org.apache.ws.security.util.WSSecurityUtil;

import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.ThrowsAdvice;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.security.wss4j.Wss4jSecurityInterceptor;

/**
 * This class is responsible for setting Acegi's security context after the request is validated. It must be used in
 * conjunction with AcegiCallbackHandler when validating a username token with a digest password.
 *
 * @author tareq.abedrabbo
 */
public class AcegiSecurityContextUpdateAdvice implements AfterReturningAdvice, ThrowsAdvice {

    public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {

        if (!(Wss4jSecurityInterceptor.class
                .isAssignableFrom(target.getClass()))) {
            throw new IllegalArgumentException(
                    "AcegiSecurityContextUpdateAdvice can only be applied to a Wss4jSecurityInterceptor");
        }

        MessageContext context = (MessageContext) args[0];

        Vector wsHandlerResults = (Vector) context
                .getProperty(WSHandlerConstants.RECV_RESULTS);

        if (wsHandlerResults != null) {
            WSHandlerResult handlerResult = (WSHandlerResult) wsHandlerResults
                    .get(0);
            Vector results = handlerResult.getResults();
            WSSecurityEngineResult actionResult = WSSecurityUtil
                    .fetchActionResult(results, WSConstants.UT);
            if (actionResult != null) {
                WSUsernameTokenPrincipal principal = (WSUsernameTokenPrincipal) actionResult
                        .getPrincipal();
                if (principal.getPasswordType().equals(WSConstants.PASSWORD_DIGEST)) {
                    String user = principal.getName();
                    String password = principal.getPassword();
                    UsernamePasswordAuthenticationToken authRequest =
                            new UsernamePasswordAuthenticationToken(user, password);
                    SecurityContextHolder.getContext().setAuthentication(authRequest);
                }
            }
        }
    }

    public void afterThrowing(Method method, Object[] args, Object target, Exception ex) throws Throwable {
        SecurityContextHolder.getContext().setAuthentication(null);
    }
}
