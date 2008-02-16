package org.springframework.ws.soap.security.wss4j;

import java.util.Properties;

import org.apache.ws.security.WSConstants;
import org.easymock.MockControl;

import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationManager;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.TestingAuthenticationToken;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.userdetails.memory.InMemoryDaoImpl;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.security.wss4j.callback.springsecurity.SpringSecurityDigestPasswordValidationCallbackHandler;
import org.springframework.ws.soap.security.wss4j.callback.springsecurity.SpringSecurityPlainTextPasswordValidationCallbackHandler;

public abstract class Wss4jMessageInterceptorSpringSecurityCallbackHandlerTestCase extends Wss4jTestCase {

    private Properties users = new Properties();

    private MockControl control;

    private AuthenticationManager mock;

    protected void onSetup() throws Exception {
        control = MockControl.createControl(AuthenticationManager.class);
        mock = (AuthenticationManager) control.getMock();
        users.setProperty("Bert", "Ernie,ROLE_TEST");
    }

    protected void tearDown() throws Exception {
        control.verify();
        SecurityContextHolder.clearContext();
    }

    public void testValidateUsernameTokenPlainText() throws Exception {
        EndpointInterceptor interceptor = prepareInterceptor("UsernameToken", true, false);
        SoapMessage message = loadMessage("usernameTokenPlainText-soap.xml");
        MessageContext messageContext = new DefaultMessageContext(message, getMessageFactory());
        interceptor.handleRequest(messageContext, null);
        assertValidateUsernameToken(message);

        // test clean up
        messageContext.getResponse();
        interceptor.handleResponse(messageContext, null);
        assertNull("Authentication created", SecurityContextHolder.getContext().getAuthentication());
    }

    public void testValidateUsernameTokenDigest() throws Exception {
        EndpointInterceptor interceptor = prepareInterceptor("UsernameToken", true, true);
        SoapMessage message = loadMessage("usernameTokenDigest-soap.xml");
        MessageContext messageContext = new DefaultMessageContext(message, getMessageFactory());
        interceptor.handleRequest(messageContext, null);
        assertValidateUsernameToken(message);

        // test clean up
        messageContext.getResponse();
        interceptor.handleResponse(messageContext, null);
        assertNull("Authentication created", SecurityContextHolder.getContext().getAuthentication());
    }

    protected void assertValidateUsernameToken(SoapMessage message) throws Exception {
        Object result = getMessage(message);
        assertNotNull("No result returned", result);
        assertXpathNotExists("Security Header not removed", "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security",
                getDocument(message));
        assertNotNull("No Authentication created", SecurityContextHolder.getContext().getAuthentication());
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
        if (digest) {
            SpringSecurityDigestPasswordValidationCallbackHandler callbackHandler =
                    new SpringSecurityDigestPasswordValidationCallbackHandler();
            InMemoryDaoImpl userDetailsService = new InMemoryDaoImpl();
            userDetailsService.setUserProperties(users);
            userDetailsService.afterPropertiesSet();
            callbackHandler.setUserDetailsService(userDetailsService);
            interceptor.setSecurementPasswordType(WSConstants.PW_DIGEST);
            interceptor.setValidationCallbackHandler(callbackHandler);
            interceptor.afterPropertiesSet();
        }
        else {
            SpringSecurityPlainTextPasswordValidationCallbackHandler callbackHandler =
                    new SpringSecurityPlainTextPasswordValidationCallbackHandler();
            Authentication authResult = new TestingAuthenticationToken("Bert", "Ernie", new GrantedAuthority[0]);
            control.expectAndReturn(mock.authenticate(new UsernamePasswordAuthenticationToken("Bert", "Ernie")),
                    authResult);
            callbackHandler.setAuthenticationManager(mock);
            callbackHandler.afterPropertiesSet();
            interceptor.setSecurementPasswordType(WSConstants.PW_TEXT);
            interceptor.setValidationCallbackHandler(callbackHandler);
            interceptor.afterPropertiesSet();
        }
        control.replay();
        return interceptor;
    }
}