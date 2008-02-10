package org.springframework.ws.soap.security.wss4j;

import java.util.Properties;

import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.TestingAuthenticationToken;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.userdetails.memory.InMemoryDaoImpl;
import org.apache.ws.security.WSConstants;
import org.easymock.MockControl;

import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.security.wss4j.callback.acegi.AcegiDigestPasswordValidationCallbackHandler;
import org.springframework.ws.soap.security.wss4j.callback.acegi.AcegiPlainTextPasswordValidationCallbackHandler;

public abstract class Wss4jMessageInterceptorAcegiCallbackHandlerTestCase extends Wss4jTestCase {

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
    }

    public void testValidateUsernameTokenPlainText() throws Exception {
        EndpointInterceptor interceptor = prepareInterceptor("UsernameToken", true, false);
        SoapMessage message = loadMessage("usernameTokenPlainText-soap.xml");
        MessageContext messageContext = new DefaultMessageContext(message, getMessageFactory());
        interceptor.handleRequest(messageContext, null);
        assertValidateUsernameToken(message);
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
        if (digest) {
            AcegiDigestPasswordValidationCallbackHandler callbackHandler =
                    new AcegiDigestPasswordValidationCallbackHandler();
            InMemoryDaoImpl userDetailsService = new InMemoryDaoImpl();
            userDetailsService.setUserProperties(users);
            userDetailsService.afterPropertiesSet();
            callbackHandler.setUserDetailsService(userDetailsService);
            interceptor.setSecurementPasswordType(WSConstants.PW_DIGEST);
            interceptor.setValidationCallbackHandler(callbackHandler);
            interceptor.afterPropertiesSet();
        }
        else {
            AcegiPlainTextPasswordValidationCallbackHandler callbackHandler =
                    new AcegiPlainTextPasswordValidationCallbackHandler();
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

