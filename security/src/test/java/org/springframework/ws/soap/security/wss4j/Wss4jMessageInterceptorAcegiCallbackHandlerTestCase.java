package org.springframework.ws.soap.security.wss4j;

import java.util.Properties;

import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.userdetails.memory.InMemoryDaoImpl;
import org.apache.ws.security.WSConstants;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.NameMatchMethodPointcutAdvisor;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.security.wss4j.callback.acegi.AcegiCallbackHandler;
import org.springframework.ws.soap.security.wss4j.callback.acegi.AcegiSecurityContextUpdateAdvice;

public abstract class Wss4jMessageInterceptorAcegiCallbackHandlerTestCase extends Wss4jTestCase {

    private Properties users = new Properties();

    protected void onSetup() throws Exception {
        users.setProperty("Bert", "Ernie,ROLE_TEST");
    }

    public void testValidateUsernameTokenDigest() throws Exception {
        EndpointInterceptor interceptor = prepareInterceptor("UsernameToken", true, true);
        SoapMessage message = loadMessage("usernameTokenDigest-soap.xml");
        MessageContext messageContext = new DefaultMessageContext(message, getMessageFactory());
        interceptor.handleRequest(messageContext, null);
        assertValidateUsernameToken(message);
    }

    protected void assertValidateUsernameToken(SoapMessage message) throws Exception {
        Object result = getMessage(message);
        assertNotNull("No result returned", result);
        assertXpathNotExists("Security Header not removed", "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security",
                getDocument(message));
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        assertNotNull("authentication must not be null", authentication);
    }

    protected EndpointInterceptor prepareInterceptor(String actions, boolean validating, boolean digest)
            throws Exception {
        Wss4jSecurityInterceptor interceptor = new Wss4jSecurityInterceptor();
        if (validating) {
            interceptor.setValidationActions(actions);
        }
        else {
            interceptor.setSecurementActions(actions);
        }
        AcegiCallbackHandler callbackHandler = new AcegiCallbackHandler();
        InMemoryDaoImpl userDetailsService = new InMemoryDaoImpl();
        userDetailsService.setUserProperties(users);
        userDetailsService.afterPropertiesSet();
        callbackHandler.setUserDetailsService(userDetailsService);
        if (digest) {
            callbackHandler.setPasswordDigestRequired(true);
            callbackHandler.setPasswordPlainTextRequired(false);
            interceptor.setSecurementPasswordType(WSConstants.PW_DIGEST);
        }
        else {
            callbackHandler.setPasswordDigestRequired(false);
            callbackHandler.setPasswordPlainTextRequired(true);
            interceptor.setSecurementPasswordType(WSConstants.PW_TEXT);
        }
        interceptor.setValidationCallbackHandler(callbackHandler);
        interceptor.afterPropertiesSet();

        ProxyFactory factory = new ProxyFactory(interceptor);
        AcegiSecurityContextUpdateAdvice advice = new AcegiSecurityContextUpdateAdvice();
        NameMatchMethodPointcutAdvisor advisor = new NameMatchMethodPointcutAdvisor(advice);
        advisor.setMappedName("handleRequest");
        factory.addAdvisor(advisor);
        return (EndpointInterceptor) factory.getProxy();
    }
}
