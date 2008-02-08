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

package org.springframework.ws.soap.security.wss4j.callback;

import java.util.Properties;

import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSecurityException;

/** @author Tareq Abed Rabbo */
public class SimpleCallbackHandler extends AbstractWss4jCallbackHandler {

    private Properties users = new Properties();

    public void setUsers(Properties users) {
        this.users = users;
    }

    public Properties getUsers() {
        return users;
    }

    protected void validateUsernameTokenPlainText(WSPasswordCallback callback) throws WSSecurityException {
        String storedPassword = users.getProperty(callback.getIdentifer());
        if (!(storedPassword != null && storedPassword.equals(callback
                .getPassword()))) {
            throw new WSSecurityException(WSSecurityException.FAILURE);
        }
    }

    protected void validateUsernameTokenDigest(WSPasswordCallback callback) throws WSSecurityException {
        callback.setPassword(users.getProperty((callback.getIdentifer())));
    }
}
