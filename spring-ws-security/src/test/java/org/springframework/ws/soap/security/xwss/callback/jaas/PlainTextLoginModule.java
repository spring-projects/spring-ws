/*
 * Copyright 2005-2014 the original author or authors.
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

package org.springframework.ws.soap.security.xwss.callback.jaas;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

public class PlainTextLoginModule implements LoginModule {

    private Subject subject;

    private CallbackHandler callbackHandler;

    private boolean success;

    private List principals = new ArrayList();

    @Override
    public boolean abort() {
        success = false;
        logout();
        return true;
    }

    @Override
    public boolean commit() throws LoginException {
        if (success) {
            if (subject.isReadOnly()) {
                throw new LoginException("Subject is read-only");
            }
            try {
                subject.getPrincipals().addAll(principals);
                principals.clear();
                return true;
            }
            catch (Exception e) {
                throw new LoginException(e.getMessage());
            }
        }
        else {
            principals.clear();
        }
        return true;
    }

    @Override
    public void initialize(Subject subject,
                           CallbackHandler callbackHandler,
                           java.util.Map sharedState,
                           java.util.Map options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
    }

    @Override
    public boolean login() throws LoginException {
        if (callbackHandler == null) {
            return false;
        }
        try {
            NameCallback nameCallback = new NameCallback("Username: ");
            PasswordCallback passwordCallback = new PasswordCallback("Password: ", false);
            Callback[] callbacks = new Callback[]{nameCallback, passwordCallback};

            callbackHandler.handle(callbacks);

            String username = nameCallback.getName();
            String password = new String(passwordCallback.getPassword());

            ((PasswordCallback) callbacks[1]).clearPassword();

            success = validate(username, password);

            callbacks[0] = null;
            callbacks[1] = null;

            if (!success) {
                throw new LoginException("Authentication failed: Password does not match");
            }

            return true;
        }
        catch (LoginException ex) {
            throw ex;
        }
        catch (Exception ex) {
            success = false;
            throw new LoginException(ex.getMessage());
        }
    }

    private boolean validate(String username, String password) {
        if ("Bert".equals(username) && "Ernie".equals(password)) {
            this.principals.add(new SimplePrincipal(username));
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public boolean logout() {
        principals.clear();

        Iterator iterator = subject.getPrincipals(SimplePrincipal.class).iterator();
        while (iterator.hasNext()) {
            SimplePrincipal principal = (SimplePrincipal) iterator.next();
            subject.getPrincipals().remove(principal);
        }

        return true;
    }


}
