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

package org.springframework.ws.soap.security.xwss.callback;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;

import com.sun.xml.wss.impl.callback.PasswordValidationCallback;
import com.sun.xml.wss.impl.callback.TimestampValidationCallback;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * Simple callback handler that validates passwords agains a in-memory <code>Properties</code> object. Password
 * validation is done on a case-sensitive basis.
 * <p/>
 * This class only handles <code>PasswordValidationCallback</code>s, and throws an
 * <code>UnsupportedCallbackException</code> for others
 *
 * @author Arjen Poutsma
 * @see #setUsers(java.util.Properties)
 */
public class SimplePasswordValidationCallbackHandler extends AbstractCallbackHandler implements InitializingBean {

    private Properties users = new Properties();

    /**
     * Sets the users to validate against. Property names are usernames, property values are passwords.
     */
    public void setUsers(Properties users) {
        this.users = users;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(users, "users is required");
    }

    protected void handleInternal(Callback callback) throws IOException, UnsupportedCallbackException {
        if (callback instanceof PasswordValidationCallback) {
            PasswordValidationCallback passwordCallback = (PasswordValidationCallback) callback;
            if (passwordCallback.getRequest() instanceof PasswordValidationCallback.PlainTextPasswordRequest) {
                passwordCallback.setValidator(new SimplePlainTextPasswordValidator());
            }
            else if (passwordCallback.getRequest() instanceof PasswordValidationCallback.DigestPasswordRequest) {
                PasswordValidationCallback.DigestPasswordRequest digestPasswordRequest =
                        (PasswordValidationCallback.DigestPasswordRequest) passwordCallback.getRequest();
                String password = users.getProperty(digestPasswordRequest.getUsername());
                digestPasswordRequest.setPassword(password);
                passwordCallback.setValidator(new PasswordValidationCallback.DigestPasswordValidator());
            }
            passwordCallback.setValidator(new SimplePlainTextPasswordValidator());
        }
        else if (callback instanceof TimestampValidationCallback) {
            TimestampValidationCallback timestampCallback = (TimestampValidationCallback) callback;
            timestampCallback.setValidator(new DefaultTimestampValidator());
        }
        else {
            throw new UnsupportedCallbackException(callback);
        }
    }

    public void setUsersMap(Map users) {
        for (Iterator iterator = users.keySet().iterator(); iterator.hasNext();) {
            String username = (String) iterator.next();
            String password = (String) users.get(username);
            this.users.setProperty(username, password);
        }
    }

    private class SimplePlainTextPasswordValidator implements PasswordValidationCallback.PasswordValidator {

        public boolean validate(PasswordValidationCallback.Request request)
                throws PasswordValidationCallback.PasswordValidationException {
            PasswordValidationCallback.PlainTextPasswordRequest plainTextPasswordRequest =
                    (PasswordValidationCallback.PlainTextPasswordRequest) request;
            String password = users.getProperty(plainTextPasswordRequest.getUsername());
            return password != null && password.equals(plainTextPasswordRequest.getPassword());
        }
    }
}
