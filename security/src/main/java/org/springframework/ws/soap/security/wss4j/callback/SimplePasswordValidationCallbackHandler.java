/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.soap.security.wss4j.callback;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSecurityException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * Simple callback handler that validates passwords agains a in-memory <code>Properties</code> object. Password
 * validation is done on a case-sensitive basis.
 *
 * @author Tareq Abed Rabbo
 * @author Arjen Poutsma
 * @see #setUsers(java.util.Properties)
 * @since 1.5.0
 */
public class SimplePasswordValidationCallbackHandler extends AbstractWsPasswordCallbackHandler
        implements InitializingBean {

    private Properties users = new Properties();

    /** Sets the users to validate against. Property names are usernames, property values are passwords. */
    public void setUsers(Properties users) {
        this.users = users;
    }

    public void setUsersMap(Map users) {
        for (Iterator iterator = users.keySet().iterator(); iterator.hasNext();) {
            String username = (String) iterator.next();
            String password = (String) users.get(username);
            this.users.setProperty(username, password);
        }
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(users, "users is required");
    }

    @Override
    protected void handleUsernameToken(WSPasswordCallback callback) throws IOException, UnsupportedCallbackException {
        String identifier = callback.getIdentifier();
        callback.setPassword(users.getProperty(identifier));
    }

    @Override
    protected void handleUsernameTokenUnknown(WSPasswordCallback callback)
            throws IOException, UnsupportedCallbackException {
        String identifier = callback.getIdentifier();
        String storedPassword = users.getProperty(identifier);
        String givenPassword = callback.getPassword();
        if (storedPassword == null || !storedPassword.equals(givenPassword)) {
            throw new WSSecurityException(WSSecurityException.FAILED_AUTHENTICATION);
        }
    }

}